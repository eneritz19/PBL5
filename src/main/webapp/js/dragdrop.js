// DRAG & DROP para subir imágenes
const dropZone = document.getElementById('dropZone');
if (dropZone) {
    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.classList.add('drag-over');
    });

    dropZone.addEventListener('dragleave', () => {
        dropZone.classList.remove('drag-over');
    });

    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('drag-over');

        const file = e.dataTransfer.files[0];
        if (file && file.type.startsWith('image/')) {
            const dataTransfer = new DataTransfer();
            dataTransfer.items.add(file);
            document.getElementById('uploadImage').files = dataTransfer.files;

            const reader = new FileReader();
            reader.onload = (event) => {
                document.getElementById('imagePreview').src = event.target.result;
                document.getElementById('imagePreview').style.display = 'block';
                document.getElementById('deletePhotoBtn').style.display = 'inline-block';
            };
            reader.readAsDataURL(file);
            showToast("Image uploaded successfully", "success");
        } else {
            showToast("Please drag only image files", "warning");
        }
    });
}