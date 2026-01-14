from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
from pathlib import Path
from PIL import Image
import io
import numpy as np
import tensorflow as tf
from tensorflow.keras.applications.efficientnet import preprocess_input

app = FastAPI(title="SkinXpert API (Keras)")

# ============================================================
# 0) CONFIG
# ============================================================
SEED = 42
np.random.seed(SEED)
tf.random.set_seed(SEED)

PROJECT_ROOT = Path(__file__).resolve().parent
MODELS_DIR = PROJECT_ROOT  # modelos al mismo nivel que main.py

IMG_SIZE = 192

MODEL1_PATH = MODELS_DIR / "model1_triage_final.keras"
MODEL2_PATH = MODELS_DIR / "model2_final.keras"
MODEL3_PATH = MODELS_DIR / "model3_best_finetuned.keras"

# ============================================================
# 1) CLASES
# ============================================================
CLASSES_M1 = ["Cancer", "Nevus", "Other_Benign"]
CLASSES_M2 = ["AK", "BCC", "MEL", "SCC"]
CLASSES_M3 = [
    "Acne_Rosacea", "Benign_Tumor", "Dermatitis", "Fungal_Infection", "Hair_Disorder",
    "Nevus", "Psoriasis", "Systemic", "Urticaria", "Viral_Infection"
]

# ============================================================
# 2) MAPEO IA
# ============================================================
DISEASE_TO_ID = {
    "AK": 2,
    "BCC": 3,
    "MEL": 4,
    "SCC": 5,

    "Acne_Rosacea": 1,
    "Benign_Tumor": 6,
    "Dermatitis": 7,
    "Fungal_Infection": 8,
    "Hair_Disorder": 9,
    "Nevus": 10,
    "Psoriasis": 11,
    "Systemic": 12,
    "Urticaria": 13,
    "Viral_Infection": 14
}


# ============================================================
# 3) UMBRALES (IGUAL QUE NOTEBOOK)
# ============================================================
P_CANCER_HIGH = 0.55
P_CANCER_LOW  = 0.25
REJECT_MARGIN = 0.05   # notebook
TOPK = 3

# ============================================================
# 4) CARGA MODELOS
# ============================================================
def _check_exists(p: Path):
    if not p.exists():
        raise FileNotFoundError(f"Modelo no encontrado: {p}")

_check_exists(MODEL1_PATH)
_check_exists(MODEL2_PATH)
_check_exists(MODEL3_PATH)

print("⏳ Cargando modelos...")
model1 = tf.keras.models.load_model(MODEL1_PATH)
model2 = tf.keras.models.load_model(MODEL2_PATH)
model3 = tf.keras.models.load_model(MODEL3_PATH)
print("✅ Modelos cargados.")

# ============================================================
# 5) HELPERS
# ============================================================
def pil_to_tensor(image: Image.Image) -> np.ndarray:
    img = image.convert("RGB").resize((IMG_SIZE, IMG_SIZE))
    arr = np.array(img).astype(np.float32)
    arr = np.expand_dims(arr, axis=0)
    arr = preprocess_input(arr)
    return arr

def looks_like_softmax(vec: np.ndarray) -> bool:
    v = np.asarray(vec).ravel()
    if np.any(v < 0) or np.any(v > 1.0):
        return False
    s = float(np.sum(v))
    return abs(s - 1.0) < 1e-2

def softmax_np(x: np.ndarray) -> np.ndarray:
    x = x.astype(np.float32)
    x = x - np.max(x, axis=-1, keepdims=True)
    e = np.exp(x)
    return e / np.sum(e, axis=-1, keepdims=True)

def probs_from_model_output(raw: np.ndarray) -> np.ndarray:
    # Si ya parece softmax -> usar tal cual, si no -> aplicar softmax
    v = np.asarray(raw).ravel()
    if looks_like_softmax(v):
        return v
    return softmax_np(v)

def topk(probs: np.ndarray, class_names: list, k: int = 3):
    probs = np.asarray(probs).ravel()
    idx = np.argsort(probs)[::-1][:k]
    return [{"label": class_names[i], "prob": float(probs[i])} for i in idx]

def reject_by_margin(probs: np.ndarray) -> bool:
    s = np.sort(np.asarray(probs).ravel())[::-1]
    if len(s) < 2:
        return True
    margin = float(s[0] - s[1])
    return margin < REJECT_MARGIN

def pipeline_predict(img_arr: np.ndarray):
    # ---------- M1: TRIAGE ----------
    raw1 = model1.predict(img_arr, verbose=0)[0]
    p1 = probs_from_model_output(raw1)

    idx = {c: i for i, c in enumerate(CLASSES_M1)}
    p_cancer = float(p1[idx["Cancer"]])
    p_nevus  = float(p1[idx["Nevus"]])
    p_other  = float(p1[idx["Other_Benign"]])

    # Decision routing (igual notebook)
    if p_cancer >= P_CANCER_HIGH:
        route = "M2"
        reason = "P(Cancer) alta"
    elif p_cancer <= P_CANCER_LOW:
        route = "M3"
        reason = "P(Cancer) baja"
    else:
        top1_idx = int(np.argmax(p1))
        top1_lab = CLASSES_M1[top1_idx]
        route = "M2" if top1_lab == "Cancer" else "M3"
        reason = f"Zona gris → clase dominante: {top1_lab}"

    # ---------- Clasificación final ----------
    if route == "M2":
        raw = model2.predict(img_arr, verbose=0)[0]
        probs = probs_from_model_output(raw)
        class_names = CLASSES_M2
    else:
        raw = model3.predict(img_arr, verbose=0)[0]
        probs = probs_from_model_output(raw)
        class_names = CLASSES_M3

    top_final = topk(probs, class_names, k=TOPK)
    pred_label = top_final[0]["label"]
    pred_conf = float(top_final[0]["prob"])          # 0..1
    pred_conf_pct = float(pred_conf * 100.0)         # 0..100

    reject = reject_by_margin(probs)

    disease_id = DISEASE_TO_ID.get(pred_label, 1)

    return {
        # CAMPOS PARA NODE-RED:
        "id_skindiseases": int(disease_id),
        "disease_name": pred_label,
        "confianza": pred_conf,                 # 0..1
        "confidence_percent": pred_conf_pct,    # 0..100
        "route_used": route,

        # INFO EXTRA DEBUG:
        "m1": {
            "p_cancer": p_cancer,
            "p_nevus": p_nevus,
            "p_other": p_other,
            "reason": reason,
            "top3": topk(p1, CLASSES_M1, 3)
        },
        "final": {
            "topk": top_final,
            "reject": reject
        }
    }

# ============================================================
# 6) ENDPOINTS
# ============================================================
@app.post("/predecir")
async def predict(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")

        img_arr = pil_to_tensor(image)
        result = pipeline_predict(img_arr)

        return JSONResponse(result)

    except Exception as e:
        return JSONResponse({"error": str(e)}, status_code=500)

@app.get("/")
async def health():
    return {"status": "SkinXpert AI running", "version": "1.1.0"}
