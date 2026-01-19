// CAMBIO 3: Funciones para cámara y eliminar foto
function previewImage(event) {
    const file = event.target.files[0];
    const preview = document.getElementById("imagePreview");
    const deleteBtn = document.getElementById("deletePhotoBtn");

    if (!file) {
        preview.style.display = "none";
        deleteBtn.style.display = "none";
        return;
    }

    const reader = new FileReader();
    reader.onload = () => {
        preview.src = reader.result;
        preview.style.display = "block";
        deleteBtn.style.display = "inline-block";
    };
    reader.readAsDataURL(file);
}

async function captureFromCamera() {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({
            video: { facingMode: 'environment' }
        });

        // Crear elemento de video para capturar
        const video = document.createElement('video');
        video.srcObject = stream;
        video.autoplay = true;
        video.playsInline = true;

        // Crear modal para la cámara
        const cameraModal = document.createElement('div');
        cameraModal.style.cssText = `
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background: rgba(0,0,0,0.95);
                    z-index: 10002;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    gap: 20px;
                `;

        video.style.cssText = `
                    max-width: 90%;
                    max-height: 70vh;
                    border-radius: 12px;
                `;

        const captureBtn = document.createElement('button');
        captureBtn.innerHTML = '<i class="fa-solid fa-camera"></i> Take Photo';
        captureBtn.style.cssText = `
                    padding: 10px 24px;
                    background: #0EA5E9;
                    color: white;
                    border: none;
                    border-radius: 12px;
                    font-size: 14px;
                    font-weight: 600;
                    cursor: pointer;
                    box-shadow: 0 4px 12px rgba(14, 165, 233, 0.4);
                `;

        const cancelBtn = document.createElement('button');
        cancelBtn.innerHTML = '<i class="fa-solid fa-times"></i> Cancel';
        cancelBtn.style.cssText = `
                    padding: 10px 24px;
                    background: #ef4444;
                    color: white;
                    border: none;
                    border-radius: 12px;
                    font-size: 14px;
                    font-weight: 600;
                    cursor: pointer;
                `;

        cameraModal.appendChild(video);
        cameraModal.appendChild(captureBtn);
        cameraModal.appendChild(cancelBtn);
        document.body.appendChild(cameraModal);

        // Función para capturar la foto
        captureBtn.onclick = () => {
            const canvas = document.createElement('canvas');
            canvas.width = video.videoWidth;
            canvas.height = video.videoHeight;
            canvas.getContext('2d').drawImage(video, 0, 0);

            canvas.toBlob((blob) => {
                const file = new File([blob], `photo_${Date.now()}.jpg`, { type: 'image/jpeg' });
                const dataTransfer = new DataTransfer();
                dataTransfer.items.add(file);
                document.getElementById('uploadImage').files = dataTransfer.files;

                // Mostrar preview
                const preview = document.getElementById("imagePreview");
                preview.src = URL.createObjectURL(blob);
                preview.style.display = "block";
                document.getElementById("deletePhotoBtn").style.display = "inline-block";

                // Cerrar modal
                stream.getTracks().forEach(track => track.stop());
                document.body.removeChild(cameraModal);
            }, 'image/jpeg', 0.95);
        };

        // Cancelar
        cancelBtn.onclick = () => {
            stream.getTracks().forEach(track => track.stop());
            document.body.removeChild(cameraModal);
        };

    } catch (error) {
        console.error('Error accessing the camera:', error);
        showToast('The camera could not be accessed. Please check your permissions.', 'error');
    }
}

function deleteSelectedPhoto() {
    const fileInput = document.getElementById('uploadImage');
    const preview = document.getElementById("imagePreview");
    const deleteBtn = document.getElementById("deletePhotoBtn");

    fileInput.value = '';
    preview.src = '';
    preview.style.display = 'none';
    deleteBtn.style.display = 'none';
}

let stream = null;

async function toggleCamera() {
    const video = document.getElementById('camera-preview');
    const btn = document.getElementById('btnCamera');

    if (video.style.display === 'none') {
        try {
            stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" } });
            video.srcObject = stream;
            video.style.display = 'block';
            btn.innerHTML = '<i class="fa-solid fa-stop"></i> Arrest';
            btn.style.backgroundColor = "#ef4444";
            btn.style.color = "#ffffff";
        } catch (e) { showToast("Camera not available", "error"); }
    } else {
        if (stream) stream.getTracks().forEach(t => t.stop());
        video.style.display = 'none';
        btn.innerHTML = '<i class="fa-solid fa-camera"></i> Start';
        btn.style.backgroundColor = "#0EA5E9";
        btn.style.color = "#000000";
    }
}