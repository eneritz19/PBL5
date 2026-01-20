import pytest
import numpy as np
import io
from PIL import Image
from unittest.mock import MagicMock, patch

# CONFIGURACION DE MOCKING 
mock_m1 = MagicMock()
mock_m2 = MagicMock()
mock_m3 = MagicMock()

mock_m1.output_shape = (None, 3)
mock_m2.output_shape = (None, 4)
mock_m3.output_shape = (None, 10)

with patch('tensorflow.keras.models.load_model', side_effect=[mock_m1, mock_m2, mock_m3]):
    from main import app, CLASSES_M1, CLASSES_M2, CLASSES_M3, DISEASE_TO_ID

from fastapi.testclient import TestClient
client = TestClient(app)

# HELPERS
def get_fake_img():
    """Genera bytes de una imagen simulada."""
    img = Image.new('RGB', (192, 192), color='red')
    buf = io.BytesIO()
    img.save(buf, format='JPEG')
    buf.seek(0)
    return buf

def set_mock_resp(model, classes, target_label, prob=0.9):
    """Configura qué clase ganará en la simulación."""
    res = np.zeros(len(classes))
    res[classes.index(target_label)] = prob
    
    remaining = (1.0 - prob) / (len(classes) - 1)
    res[res == 0] = remaining
    model.predict.return_value = [res.tolist()]

# TESTS DE LOGICA
def test_logic_id_ak():
    """Si M1=Cancer y M2=AK, debe devolver ID 2. (Prueba de sabotaje)"""
    set_mock_resp(mock_m1, CLASSES_M1, "Cancer", 0.9)
    set_mock_resp(mock_m2, CLASSES_M2, "AK", 0.9)
    
    response = client.post("/predecir", files={"file": ("t.jpg", get_fake_img(), "image/jpeg")})
    data = response.json()
    
    
    assert data["id_skindiseases"] == 2 
    assert data["disease_name"] == "AK"

def test_logic_route_cancer_m2():
    """
    TEST: Si M1 dice Cáncer, ¿va a M2 y devuelve urgencia ALTA?
    Este test asegura que el 'triage' no desvíe casos graves a la ruta benigna.
    """
    set_mock_resp(mock_m1, CLASSES_M1, "Cancer", 0.9)
    set_mock_resp(mock_m2, CLASSES_M2, "MEL", 0.95) # Melanoma
    
    response = client.post("/predecir", files={"file": ("t.jpg", get_fake_img(), "image/jpeg")})
    data = response.json()
    
    assert data["route_used"] == "M2"
    assert data["disease_name"] == "MEL"
    assert data["urgency_label"] == "ALTO"
    assert data["id_skindiseases"] == DISEASE_TO_ID["MEL"]

def test_logic_route_benign_m3():
    """
    TEST: Si M1 dice Nevus, ¿va a M3 y devuelve el ID correcto?
    Valida que la ruta de enfermedades comunes funciona y mapea bien los IDs.
    """
    set_mock_resp(mock_m1, CLASSES_M1, "Nevus", 0.9)
    set_mock_resp(mock_m3, CLASSES_M3, "Psoriasis", 0.85)
    
    response = client.post("/predecir", files={"file": ("t.jpg", get_fake_img(), "image/jpeg")})
    data = response.json()
    
    assert data["route_used"] == "M3"
    assert data["disease_name"] == "Psoriasis"
    assert data["id_skindiseases"] == DISEASE_TO_ID["Psoriasis"]

def test_logic_reject_low_confidence():
    """
    TEST: Si la confianza es baja o esta muy reñida, ¿marca REVISAR?
    Es el test de seguridad para evitar diagnósticos falsos por azar.
    """
    set_mock_resp(mock_m1, CLASSES_M1, "Cancer", 0.9)
    
    set_mock_resp(mock_m2, CLASSES_M2, "BCC", 0.40) 
    
    response = client.post("/predecir", files={"file": ("t.jpg", get_fake_img(), "image/jpeg")})
    assert response.json()["status"] == "REVISAR"

def test_logic_json_schema():
    """
    TEST: ¿El JSON tiene todos los campos que Node-RED necesita?
    Evita que un cambio en el codigo rompa la integración con el resto del equipo.
    """
    set_mock_resp(mock_m1, CLASSES_M1, "Cancer", 0.9)
    set_mock_resp(mock_m2, CLASSES_M2, "AK", 0.9)
    
    response = client.post("/predecir", files={"file": ("t.jpg", get_fake_img(), "image/jpeg")})
    data = response.json()
    required = ["id_skindiseases", "confianza", "disease_name", "urgency_label", "status"]
    for field in required:
        assert field in data