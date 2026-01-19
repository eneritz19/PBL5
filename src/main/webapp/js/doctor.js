// CAMBIO 5: Imagen clickeable en pending
async function loadDoctorPending() {
    const doctorId = localStorage.getItem("doctor_id");
    const res = await fetch(`${API}/doctor/pending?id_doctor=${doctorId}`);
    const cases = await res.json();
    const list = document.getElementById("doctorPendingList");
    list.innerHTML = "";

    const AHORA = Date.now();
    const TREINTA_MINUTOS_MS = 30 * 60 * 1000;

    cases.forEach(c => {
        let badgeClass = 'badge-low';
        let cardClass = 'doctor-urgency-low';
        let alertaRetraso = "";

        if (c.urgency === 3) { badgeClass = 'badge-high'; cardClass = 'urgency-high'; }
        else if (c.urgency === 2) { badgeClass = 'badge-medium'; cardClass = 'urgency-medium'; }

        const tiempoEsperaMs = AHORA - c.ia_confidence_millis;
        const minutosEspera = Math.floor(tiempoEsperaMs / (1000 * 60));

        if (tiempoEsperaMs > TREINTA_MINUTOS_MS) {
            alertaRetraso = `
                <div style="background: #fee2e2; color: #b91c1c; padding: 8px; border-radius: 8px; margin-top: 10px; font-size: 13px; font-weight: bold; border: 1px solid #f87171;">
                    <i class="fa-solid fa-triangle-exclamation"></i> 
                    ALERTA: Paciente esperando hace ${minutosEspera} min.
                </div>`;
        }

        const imagePath = `http://localhost:8080${c.file_path}`;

        list.innerHTML += `
            <div class="case-card ${cardClass}">
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <h3>${c.name}</h3>
                    <span class="urgency-badge ${badgeClass}">${Math.round(c.ia_confidence)}% AI RISK</span>
                </div>
                
                <img src="${imagePath}" 
                     class="clickable-image" 
                     onclick="openImageModal('${imagePath}')"
                     style="width:100%; height:200px; object-fit:cover; margin: 15px 0; border-radius:12px; border: 1px solid #eee;">
                
                <div style="background: #f0f9ff; padding: 10px; border-radius: 8px; margin-bottom: 5px; border-left: 4px solid #0ea5e9;">
                    <p style="margin:0; font-size:12px; color:#0369a1; font-weight:bold;">AI SUGGESTION:</p>
                    <p style="margin:0; font-size:15px; color:#1e40af;">${c.ia_suggestion || 'Analyzing...'}</p>
                </div>

                ${alertaRetraso}
                
                <div style="margin-top:15px;">
                    <label style="font-size:12px; font-weight:bold;">Final Diagnosis:</label>
                    <select id="disease-${c.id_request}" style="width:100%; padding:10px; margin: 8px 0;">
                        <option value="1">Acne Rosacea</option>
                        <option value="2">Actinic Keratosis</option>
                        <option value="3">Basal Cell Carcinoma</option>
                        <option value="4">Melanoma</option>
                        <option value="5">Squamous Cell Carcinoma</option>
                        <option value="6">Benign Tumor</option>
                        <option value="7">Dermatitis</option>
                        <option value="8">Fungal Infection</option>
                        <option value="9">Hair Disorder</option>
                        <option value="10">Nevus</option>
                        <option value="11">Psoriasis</option>
                        <option value="12">Systemic Disease</option>
                        <option value="13">Urticaria</option>
                        <option value="14">Viral Infection</option>
                    </select>
                    <textarea id="notes-${c.id_request}" placeholder="Treatment..." rows="2" style="width:100%; padding:10px;" maxlength="500" oninput="updateCharCount(this)"></textarea>
                    <input type="datetime-local" id="appointment-${c.id_request}" style="width:100%; padding:10px; margin-top:10px;">
                    <button onclick="sendDiagnosis(${c.id_request}, ${doctorId}, ${c.id_patient})" style="margin-top:10px;">Complete Review</button>
                </div>
            </div>`;
    });
    initScrollAnimations();
}

async function loadDoctorPatients() {
    const doctorId = localStorage.getItem("doctor_id");
    try {
        const res = await fetch(`${API}/doctor/patients?id_doctor=${doctorId}`);
        const patients = await res.json();
        const list = document.getElementById("doctorPatientsList");
        list.innerHTML = "";

        patients.forEach(p => {
            list.innerHTML += `
                <div class="case-card reveal-on-scroll" style="display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <strong>${p.name}</strong><br>
                        <span style="font-size:14px; color:#888;">ID: ${p.dni} • ${p.email}</span>
                    </div>
                </div>`;
        });
        initScrollAnimations();
    } catch (err) { console.error(err); }
}

async function loadDoctorAppointments() {
    const doctorId = localStorage.getItem("doctor_id");
    try {
        const res = await fetch(`${API}/doctor/appointments?id_doctor=${doctorId}`);
        const citas = await res.json();
        const list = document.getElementById("doctorAppointmentsList");
        list.innerHTML = "";

        if (!citas.length) {
            list.innerHTML = `<div class="case-card reveal-on-scroll"><p style="color:#888;">No hay citas programadas.</p></div>`;
            return;
        }

        citas.forEach(c => {
            const fecha = new Date(c.date);
            const dia = fecha.getDate().toString().padStart(2, '0');
            const mes = fecha.toLocaleString('es-ES', { month: 'short' }).toUpperCase();
            const hora = fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });

            let estadoColor = "#999";
            if (c.status === 'pending') estadoColor = "#d39e00";
            if (c.status === 'done') estadoColor = "#28a745";
            if (c.status === 'cancel') estadoColor = "#dc3545";

            list.innerHTML += `
                <div class="case-card reveal-on-scroll" style="display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <strong>${c.patient_name}</strong><br>
                        <span style="font-size:14px;">${dia} ${mes} • ${hora}</span><br>
                        <span style="font-size:13px; color:#888; margin-top:8px; display:inline-block;">Status: <b style="color:${estadoColor};">${c.status}</b></span>
                    </div>
                    <div style="display:flex; gap:10px;">
                        <button class="btn-secondary" onclick="editAppointment(${c.id_appointment}, '${c.date}', ${c.id_patient})" style="width:auto; padding:10px 20px;">Edit</button>
                        <button class="btn-secondary delete-btn" style="width:auto; padding:10px 20px;" onclick="deleteAppointment(${c.id_appointment}, ${c.id_patient})">Delete</button>
                    </div>
                </div>`;
        });
        initScrollAnimations();
    } catch (err) { console.error(err); }
}

async function sendDiagnosis(reqId, docId, patId) {
    const diseaseSelect = document.getElementById(`disease-${reqId}`);
    const notesInput = document.getElementById(`notes-${reqId}`);
    const appointmentInput = document.getElementById(`appointment-${reqId}`);

    const diseaseId = diseaseSelect.value;
    const notes = notesInput.value;

    if (!notes) {
        showToast("Please write some notes for the patient", "warning");
        return;
    }

    const payload = {
        id_request: reqId,
        id_doctor: docId,
        id_patient: patId,
        id_skindiseases: parseInt(diseaseId),
        confidence: 100,
        doctor_notes: notes,
        appointment_date: appointmentInput.value
    };

    showLoading("Sending diagnosis...");

    try {
        const res = await fetch(`${API}/doctor/send_report`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const result = await res.json();
        hideLoading();

        if (res.ok) {
            showToast("Report sent successfully", "success");
            loadDoctorPending();
            loadDoctorAppointments();
        } else {
            showToast("Error: " + JSON.stringify(result), "error");
        }
    } catch (e) {
        console.error(e);
        hideLoading();
        showToast("Connection error sending the report", "error");
    }
}


