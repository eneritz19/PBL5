async function loadPatientHistory() {
    const patientId = localStorage.getItem("patient_id");
    try {
        const res = await fetch(`${API}/patient/history?id_patient=${patientId}`);
        const history = await res.json();
        const list = document.getElementById("patientHistoryList");
        list.innerHTML = "";

        // Filtrar solo los casos que tienen doctor_notes (ya fueron revisados por el doctor)
        let completedCases = history.filter(h => h.doctor_notes && h.doctor_notes.trim() !== '');

        // Eliminar duplicados: Si hay varios registros con la misma imagen (file_path),
        // solo quedarse con UNO (el más reciente o el que tenga confidence = 100 del doctor)
        const uniqueCases = {};
        completedCases.forEach(h => {
            const key = h.file_path || h.id_request; // Usar file_path como identificador único

            // Si ya existe este caso, comparar y quedarse con el mejor
            if (uniqueCases[key]) {
                // Priorizar el que tiene confidence = 100 (diagnóstico del doctor)
                if (h.confidence === 100 && uniqueCases[key].confidence !== 100) {
                    uniqueCases[key] = h;
                }
                // Si ambos tienen confidence 100, quedarse con el más reciente
                else if (h.confidence === 100 && uniqueCases[key].confidence === 100) {
                    const currentDate = new Date(h.diagnosis_date);
                    const existingDate = new Date(uniqueCases[key].diagnosis_date);
                    if (currentDate > existingDate) {
                        uniqueCases[key] = h;
                    }
                }
            } else {
                uniqueCases[key] = h;
            }
        });

        // Convertir el objeto de vuelta a array
        completedCases = Object.values(uniqueCases);

        if (!completedCases.length) {
            list.innerHTML = `<div class="case-card"><p style="color:#888;">No medical history yet. Your doctor will review your consultation soon.</p></div>`;
            return;
        }

        completedCases.forEach(h => {
            const fecha = new Date(h.diagnosis_date).toLocaleDateString("en-US", {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
            const imagePath = h.file_path ? `http://localhost:8080${h.file_path}` : 'https://via.placeholder.com/100';

            list.innerHTML += `
            <div class="case-card urgency-low">
                <p style="font-size:14px; color:#888;">${fecha}</p>
                <div style="display:flex; gap:15px; margin-top:10px;">
                    <img src="${imagePath}" style="width:80px; height:80px; border-radius:8px; object-fit: cover; cursor: pointer;" onclick="openImageModal('${imagePath}')">
                    <div>
                        <strong>${h.disease || 'Diagnosis'}</strong>
                        <p style="margin-top:5px; font-size:14px;">${h.doctor_notes}</p>
                    </div>
                </div>
            </div>`;
        });
        initScrollAnimations();
    } catch (err) { console.error(err); }
}

async function loadPatientAppointments() {
    const patientId = localStorage.getItem("patient_id");
    try {
        const res = await fetch(`${API}/patient/appointments?id_patient=${patientId}`);
        const citas = await res.json();
        const list = document.getElementById("patientAppointmentsList");
        list.innerHTML = "";

        if (!citas.length) {
            list.innerHTML = `<div class="case-card"><p style="color:#888;">You have no upcoming appointments.</p></div>`;
            return;
        }

        citas.forEach(c => {
            const fecha = new Date(c.date);
            const dia = fecha.getDate().toString().padStart(2, '0');
            const mes = fecha.toLocaleString('es-ES', { month: 'short' }).toUpperCase();
            const hora = fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });

            list.innerHTML += `
                <div class="case-card urgency-medium">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <div>
                            <h4 style="margin:0;">Cita con ${c.doctor_name}</h4>
                            <p style="margin:5px 0 0 0; color:#64748B;">Status: ${c.status}</p>
                        </div>
                        <div style="text-align:right;">
                            <span style="font-weight:bold; color:var(--sk-blue-dark);">${dia} ${mes}</span><br>
                            <span style="font-size:14px;">${hora}</span>
                        </div>
                    </div>
                </div>`;
        });
        initScrollAnimations();
    } catch (err) { console.error(err); }
}