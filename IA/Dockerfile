# Dockerfile en la carpeta /skinxpert_ai
FROM python:3.9-slim

WORKDIR /app

# Instalar dependencias del sistema si son necesarias para tus librerías de IA
# RUN apt-get update && apt-get install -y libgl1-mesa-glx

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar el código y el modelo
COPY . .

# Exponer el puerto donde correrá uvicorn (interno de Docker)
EXPOSE 5000

# Comando para iniciar la app
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5000"]