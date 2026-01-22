import pytest
from fastapi.testclient import TestClient
from PIL import Image
import io
import numpy as np
from main import app 

client = TestClient(app)

def test_real_health_check():
    """Prueba que la API responde y los modelos están cargados en RAM."""
    response = client.get("/")
    assert response.status_code == 200
    assert "M1" in response.json()["models_loaded"]

def test_real_prediction_flow():
    """
    Prueba que una imagen real pasa por EfficientNet y los modelos reales.
    Valida que no hay errores de 'Shape' (dimensiones) ni de tipos de datos.
    """
    # Crear imagen aleatoria
    arr = np.random.randint(0, 255, (192, 192, 3), dtype=np.uint8)
    img = Image.fromarray(arr)
    buf = io.BytesIO()
    img.save(buf, format='JPEG')
    buf.seek(0)
    
    response = client.post("/predecir", files={"file": ("real.jpg", buf, "image/jpeg")})
    
    assert response.status_code == 200
    data = response.json()
    assert 0.0 <= data["confianza"] <= 1.0

def test_real_error_handling():
    """Prueba que el try/except de IA.py captura archivos basura sin tumbar el servidor."""
    response = client.post("/predecir", files={"file": ("bad.txt", io.BytesIO(b"not-an-image"), "text/plain")})
    assert response.status_code == 500
    assert "error" in response.json()