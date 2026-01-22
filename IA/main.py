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
# 1) CLASES (FIJAS) — IMPORTANTE: mismo orden que class_indices
# ============================================================
CLASSES_M1 = ["Cancer", "Nevus", "Other_Benign"]
CLASSES_M2 = ["AK", "BCC", "MEL", "SCC"]
CLASSES_M3 = [
    "Acne_Rosacea", "Benign_Tumor", "Dermatitis", "Fungal_Infection", "Hair_Disorder",
    "Nevus", "Psoriasis", "Systemic", "Urticaria", "Viral_Infection"
]

#============================================================
# 2) MAPEO DE PREDICCIÓN A ID DE BASE DE DATOS
# ============================================================
DISEASE_TO_ID = {
    # Modelo 2 (cánceres y lesiones premalignas)
    "AK": 2,        # Actinic Keratosis
    "BCC": 3,       # Basal Cell Carcinoma
    "MEL": 4,       # Melanoma
    "SCC": 5,       # Squamous Cell Carcinoma
    
    # Modelo 3 (benignas y otras enfermedades)
    "Acne_Rosacea": 1,       # Acne and Rosacea
    "Benign_Tumor": 6,       # Benign Tumor
    "Dermatitis": 7,         # Dermatitis
    "Fungal_Infection": 8,   # Fungal Infection
    "Hair_Disorder": 9,      # Hair Disorder
    "Nevus": 10,             # Nevus
    "Psoriasis": 11,         # Psoriasis
    "Systemic": 12,          # Systemic Disease
    "Urticaria": 13,         # Urticaria
    "Viral_Infection": 14    # Viral Infection
}

# ============================================================
# 3) URGENCIA / RIESGO
# ============================================================
HIGH_URGENCY = {"MEL", "SCC"}
MEDIUM_URGENCY = {"BCC", "AK"}
LOW_BENIGN = {"Nevus", "Benign_Tumor", "Acne_Rosacea"}
MEDIUM_BENIGN = {"Fungal_Infection", "Systemic", "Viral_Infection"}

# ============================================================
# 4) UMBRALES DE CONFIANZA (REJECT)
# ============================================================
THRESH_M1_CANCER = 0.35
REJECT_TOP1 = 0.45
REJECT_MARGIN = 0.10
TOPK = 3

# ============================================================
# 5) CARGA MODELOS (global)
# ============================================================
print("🔍 Buscando modelos en:", MODELS_DIR)
print("=" * 60)

# ✅ VERIFICACIÓN MEJORADA
if not MODEL1_PATH.exists():
    print(f"NO ENCONTRADO: {MODEL1_PATH}")
    print(f"   Contenido de {MODELS_DIR}:")
    for f in MODELS_DIR.iterdir():
        print(f"   - {f.name}")
else:
    print(f"Encontrado: {MODEL1_PATH}")

if not MODEL2_PATH.exists():
    print(f"NO ENCONTRADO: {MODEL2_PATH}")
else:
    print(f"Encontrado: {MODEL2_PATH}")

if not MODEL3_PATH.exists():
    print(f"NO ENCONTRADO: {MODEL3_PATH}")
else:
    print(f"Encontrado: {MODEL3_PATH}")

print("=" * 60)
print("⏳ Cargando modelos en memoria...")

model1 = tf.keras.models.load_model(MODEL1_PATH)
model2 = tf.keras.models.load_model(MODEL2_PATH)
model3 = tf.keras.models.load_model(MODEL3_PATH)

print("Modelos cargados correctamente.")
print(f"   M1 output: {model1.output_shape} | classes: {len(CLASSES_M1)}")
print(f"   M2 output: {model2.output_shape} | classes: {len(CLASSES_M2)}")
print(f"   M3 output: {model3.output_shape} | classes: {len(CLASSES_M3)}")
print("=" * 60)


# ============================================================
# 6) HELPERS
# ============================================================
def pil_to_tensor(image: Image.Image) -> np.ndarray:
    """PIL -> np array (1, IMG_SIZE, IMG_SIZE, 3) preprocesado como EfficientNet."""
    img = image.convert("RGB").resize((IMG_SIZE, IMG_SIZE))
    arr = np.array(img).astype(np.float32)
    arr = np.expand_dims(arr, axis=0)
    arr = preprocess_input(arr)
    return arr

def softmax_np(x: np.ndarray) -> np.ndarray:
    """Aplica softmax manualmente a un array."""
    x = x.astype(np.float32)
    x = x - np.max(x, axis=-1, keepdims=True)
    e = np.exp(x)
    return e / np.sum(e, axis=-1, keepdims=True)

def looks_like_softmax(vec: np.ndarray) -> bool:
    """Verifica si un vector parece ser resultado de softmax (suma ~1, valores 0-1)"""
    v = np.asarray(vec).ravel()
    if np.any(v < 0) or np.any(v > 1.0):
        return False
    s = float(np.sum(v))
    return abs(s - 1.0) < 1e-2

def probs_from_model_output(raw: np.ndarray) -> np.ndarray:
    """
    Si ya parece softmax → usar tal cual
    Si no → aplicar softmax
    Esto evita aplicar softmax dos veces y distorsionar las probabilidades.
    """
    v = np.asarray(raw).ravel()
    if looks_like_softmax(v):
        return v
    return softmax_np(v)

def topk(probs: np.ndarray, class_names: list, k: int = 3):
    """Devuelve las top-k predicciones con sus probabilidades."""
    probs = np.asarray(probs).ravel()
    idx = np.argsort(probs)[::-1][:k]
    return [{"label": class_names[i], "prob": float(probs[i])} for i in idx]

def reject_rule(probs: np.ndarray) -> bool:
    """Reject si el modelo está poco seguro."""
    s = np.sort(probs)[::-1]
    top1 = s[0]
    top2 = s[1] if len(s) > 1 else 0.0
    if top1 < REJECT_TOP1:
        return True
    if (top1 - top2) < REJECT_MARGIN:
        return True
    return False

def urgency_from_result(stage: str, label: str) -> str:
    """Devuelve ALTO / MEDIO / BAJO según predicción final."""
    if stage == "M2":
        if label in HIGH_URGENCY:
            return "ALTO"
        if label in MEDIUM_URGENCY:
            return "MEDIO"
        return "MEDIO"
    if stage == "M3":
        if label in LOW_BENIGN:
            return "BAJO"
        if label in MEDIUM_BENIGN:
            return "MEDIO"
        return "MEDIO"
    return "MEDIO"

def pipeline_predict(img_arr: np.ndarray):
    """
    Pipeline completo:
      1) M1 triage => decide ruta (M2 vs M3)
      2) M2 o M3 => predicción final
    """

    # --- M1 TRIAGE ---
    raw1 = model1.predict(img_arr, verbose=0)[0]
    probs1 = probs_from_model_output(raw1)  # ✅ Verifica antes de aplicar softmax
    top1_m1 = topk(probs1, CLASSES_M1, k=3)

    p_cancer = float(probs1[CLASSES_M1.index("Cancer")])
    pred_m1 = CLASSES_M1[int(np.argmax(probs1))]

    # Decisión de ruta
    if pred_m1 == "Cancer" or p_cancer >= THRESH_M1_CANCER:
        route = "M2"
        raw2 = model2.predict(img_arr, verbose=0)[0]
        probs2 = probs_from_model_output(raw2)  # Verifica antes de aplicar softmax
        top_final = topk(probs2, CLASSES_M2, k=TOPK)
        pred_final_label = top_final[0]["label"]
        pred_confidence = top_final[0]["prob"]
        reject = reject_rule(probs2)
        urgency = urgency_from_result("M2", pred_final_label)
        explanation = f"M1 detecta Cancer (p={p_cancer:.3f}) → se usa M2 (tipo de cáncer)."
    else:
        route = "M3"
        raw3 = model3.predict(img_arr, verbose=0)[0]
        probs3 = probs_from_model_output(raw3)  # Verifica antes de aplicar softmax
        top_final = topk(probs3, CLASSES_M3, k=TOPK)
        pred_final_label = top_final[0]["label"]
        pred_confidence = top_final[0]["prob"]
        reject = reject_rule(probs3)
        urgency = urgency_from_result("M3", pred_final_label)
        explanation = f"M1 NO detecta Cancer (p={p_cancer:.3f}) → se usa M3 (benigno/enfermedades)."

    if reject:
        status = "REVISAR"
    else:
        status = "OK"

    disease_id = DISEASE_TO_ID.get(pred_final_label, 1)

    return {
        # CAMPOS PRINCIPALES PARA NODE-RED:
        "id_skindiseases": disease_id,
        "confianza": pred_confidence,  # Ahora será 0-1 correctamente (no 0.5 fijo)
        "disease_name": pred_final_label,
        "urgency_label": urgency,
        
        # INFORMACIÓN ADICIONAL:
        "status": status,
        "route_used": route,
        "m1": {
            "p_cancer": p_cancer,
            "top3": top1_m1,
            "pred": pred_m1
        },
        "final": {
            "pred": pred_final_label,
            "topk": top_final,
            "urgency": urgency,
            "reject": reject
        },
        "decision_explanation": explanation
    }

# ============================================================
# 7) ENDPOINTS
# ============================================================
@app.post("/predecir")
async def predict(file: UploadFile = File(...)):
    """
    Endpoint principal para predicción de enfermedades de piel.
    Recibe una imagen y devuelve el diagnóstico con confianza.
    """
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
    """Health check endpoint."""
    return {
        "status": "SkinXpert AI running",
        "models_loaded": ["M1", "M2", "M3"],
        "version": "1.1.0"
    }