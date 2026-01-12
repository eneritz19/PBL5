from fastapi import FastAPI, File, UploadFile
from PIL import Image
import io
import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import transforms, models
import numpy as np

app = FastAPI()

# ==========================================
# 1. CONFIGURACIÓN DEL DISPOSITIVO
# ==========================================
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print(f"--- Servidor SkinXpert usando: {device} ---")

# ==========================================
# 2. DEFINICIÓN DE LAS 23 CLASES Y MAPEO SQL
# ==========================================
# El orden debe ser EXACTAMENTE el mismo que en tu entrenamiento (alfabético)
CLASES = [
    "Acne and Rosacea Photos", "Actinic Keratosis Basal Cell Carcinoma and other Malignant Lesions",
    "Atopic Dermatitis Photos", "Bullous Disease Photos", "Cellulitis Impetigo and other Bacterial Infections",
    "Eczema Photos", "Exanthems and Drug Eruptions", "Hair Loss Photos Alopecia and other Hair Diseases",
    "Herpes HPV and other STDs Photos", "Light Diseases and Disorders of Pigmentation",
    "Lupus and other Connective Tissue diseases", "Melanoma Skin Cancer Nevi and Moles",
    "Nail Fungus and other Nail Disease", "Poison Ivy Photos and other Contact Dermatitis",
    "Psoriasis pictures Lichen Planus and related diseases", "Scabies Lyme Disease and other Infestations and Bites",
    "Seborrheic Keratoses and other Benign Tumors", "Systemic Disease",
    "Tinea Ringworm Candidiasis and other Fungal Infections", "Urticaria Hives",
    "Vascular Tumors", "Vasculitis Photos", "Warts Molluscum and other Viral Infections"
]

# Mapeo a los IDs de tu base de datos MySQL (basado en el orden de tu tabla de enfermedades)
MAPA_SQL = {clase: i + 1 for i, clase in enumerate(CLASES)}

# ==========================================
# 3. RECONSTRUCCIÓN DE LA ARQUITECTURA (EfficientNet-B0)
# ==========================================
def cargar_modelo():
    print("Iniciando reconstrucción de la arquitectura B0...")
    # 1. Creamos la base
    model = models.efficientnet_b0(weights=None) 
    
    # 2. Recreamos la "cabeza" que entrenamos (debe ser idéntica)
    num_ftrs = model.classifier[1].in_features
    model.classifier[1] = nn.Sequential(
        nn.Dropout(p=0.4, inplace=True),
        nn.Linear(num_ftrs, 23)
    )
    
    # 3. Cargar los pesos entrenados
    MODEL_PATH = 'mejor_modelo_skinxpert.pth'
    try:
        # Cargamos el state_dict (los pesos)
        state_dict = torch.load(MODEL_PATH, map_location=device)
        model.load_state_dict(state_dict)
        model.to(device)
        model.eval()
        print("✅ Pesos del modelo cargados correctamente.")
        return model
    except Exception as e:
        print(f"❌ ERROR al cargar pesos: {e}")
        return None

# Instanciamos el modelo globalmente
skinxpert_model = cargar_modelo()

# ==========================================
# 4. PREPROCESAMIENTO DE IMAGEN
# ==========================================
transform_pipeline = transforms.Compose([
    transforms.Resize((448, 448)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

# ==========================================
# 5. ENDPOINT PARA NODE-RED
# ==========================================
@app.post("/predecir")
async def predict(file: UploadFile = File(...)):
    if skinxpert_model is None:
        return {"error": "Modelo no disponible en el servidor."}

    try:
        # Leer y convertir imagen
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert('RGB')
        
        # Aplicar transformaciones
        tensor = transform_pipeline(image).unsqueeze(0).to(device)

        # Inferencia
        with torch.no_grad():
            outputs = skinxpert_model(tensor)
            probabilities = F.softmax(outputs, dim=1)
            confianza, pred_idx = torch.max(probabilities, 1)

        idx = pred_idx.item()
        nombre_clase = CLASES[idx]
        id_sql = MAPA_SQL.get(nombre_clase, -1)

        # Determinar riesgo
        malignas = ["Melanoma", "Actinic", "Basal Cell"]
        categoria = "Maligno/Precanceroso" if any(x in nombre_clase for x in malignas) else "Benigno"

        print(f"🔍 Predicción: {nombre_clase} | Confianza: {confianza.item():.2%}")

        return {
            "id_skindiseases": id_sql,
            "diagnostico_texto": nombre_clase,
            "confianza": float(confianza.item()),
            "categoria_general": categoria
        }

    except Exception as e:
        return {"error": f"Error en procesamiento: {str(e)}"}

# Ejecución: uvicorn main:app --host 0.0.0.0 --port 8000