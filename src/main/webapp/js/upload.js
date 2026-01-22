async function uploadCase() {
    const fileInput = document.getElementById("uploadImage");
    const patientId = localStorage.getItem("patient_id");
    const uploadBtn = document.getElementById("uploadBtn");

    if (fileInput.files.length === 0) {
        showToast("Please select an image first", "warning");
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append("foto", file);
    formData.append("id_patient", patientId);

    uploadBtn.disabled = true;
    uploadBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Sending...';
    showLoading("Analyzing with AI...");

    try {
        const response = await fetch(`${API}/upload-case`, {
            method: "POST",
            body: formData
        });

        hideLoading();

        if (response.ok) {
            showToast("Consultation sent. AI is analyzing your case", "success");
            deleteSelectedPhoto();
            //loadPatientHistory();
        } else {
            showToast("Error sending query", "error");
        }
    } catch (error) {
        console.error("Error:", error);
        hideLoading();
        showToast("Unable to connect to the server", "error");
    } finally {
        uploadBtn.disabled = false;
        uploadBtn.innerHTML = 'Send Inquiry';
    }
}