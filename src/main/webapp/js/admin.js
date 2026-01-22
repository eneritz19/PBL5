let currentType = null;
let isEditing = false;

async function loadAdminDoctors() {
    const res = await fetch(`${API}/admin/doctors`);
    const doctors = await res.json();
    const list = document.getElementById("adminDoctorsList");
    list.innerHTML = "";

    doctors.forEach(d => {
        list.innerHTML += `
        <div class="case-card" style="display:flex;justify-content:space-between; align-items:center;">
            <div>
                <strong>${d.name}</strong><br>
                <span style="color:#888">${d.email} • ${d.clinic || ''}</span>
            </div>
            <div class="admin-button-group">
                <button class="btn-secondary" onclick="editDoctor(${d.id_doctor})">
                    <i class="fa-solid fa-edit"></i> Edit
                </button>
                <button class="btn-secondary" style="color:red;" onclick="deleteDoctor(${d.id_doctor})">
                    <i class="fa-solid fa-trash"></i> Delete
                </button>   
            </div>
        </div>`;
    });
}

async function loadAdminPatients() {
    const res = await fetch(`${API}/admin/patients`);
    const patients = await res.json();
    const list = document.getElementById("adminPatientsList");
    list.innerHTML = "";

    patients.forEach(p => {
        list.innerHTML += `
        <div class="case-card" style="display:flex;justify-content:space-between; align-items:center;">
            <div>
                <strong>${p.name}</strong><br>
                <span style="color:#888">${p.email} • Doctor: ${p.doctor || '—'}</span>
            </div>
            <div class="admin-button-group">
                <button class="btn-secondary" onclick="editPatient(${p.id_patient})">
                    <i class="fa-solid fa-edit"></i> Edit
                </button>
                <button class="btn-secondary" style="color:red;" onclick="deletePatient(${p.id_patient})">
                    <i class="fa-solid fa-trash"></i> Delete
                </button>
            </div>
        </div>`;
    });
}

async function deleteDoctor(id) {
    showConfirm(
        "Delete Doctor",
        "Are you sure you want to delete this doctor and ALL related data?",
        "",
        async () => {
            showLoading("Deleting...");
            try {
                const res = await fetch(`${API}/admin/doctor/${id}`, { method: "DELETE" });
                const data = await res.json();

                hideLoading();

                if (res.ok) {
                    showToast("Doctor successfully removed", "success");
                    loadAdminDoctors();
                    loadAdminPatients();
                } else {
                    showToast("Error deleting doctor", "error");
                }
            } catch (err) {
                console.error(err);
                hideLoading();
                showToast("Error deleting doctor", "error");
            }
        }
    );
}

async function deletePatient(id) {
    showConfirm(
        "Remove Patient",
        "Are you sure you want to delete this patient and ALL related data?",
        "",
        async () => {
            showLoading("Deleting...");
            try {
                const res = await fetch(`${API}/admin/patient/${id}`, { method: "DELETE" });
                const data = await res.json();

                hideLoading();

                if (res.ok) {
                    showToast("Patient successfully removed", "success");
                    loadAdminPatients();
                } else {
                    showToast("Error deleting patient", "error");
                }
            } catch (err) {
                console.error(err);
                hideLoading();
                showToast("Error deleting patient", "error");
            }
        }
    );
}

function openNewDoctor() {
    document.getElementById("modal-title").innerText = "New Doctor";
    document.getElementById("modal-body").innerHTML = `
                <input id="new_doctor_code" placeholder="Doctor Code">
                <input id="new_doctor_name" placeholder="Full Name">
                <input id="new_doctor_email" placeholder="Email">
                <input id="new_doctor_password" type="password" placeholder="Temporary Password">
                <input id="new_doctor_clinic" placeholder="Clinic ID (optional)">
            `;
    openModal("doctor", false);
}

function openNewPatient() {
    document.getElementById("modal-title").innerText = "New Paciente";
    document.getElementById("modal-body").innerHTML = `
                <input id="new_patient_dni" placeholder="ID card / National Identity Card">
                <input id="new_patient_name" placeholder="Full Name">
                <input id="new_patient_email" placeholder="Email">
                <input id="new_patient_password" type="password" placeholder="Temporary Password">
                <select id="new_patient_doctor">
                    <option value="">No doctor assigned</option>
                </select>
            `;
    openModal("patient", false);
    loadDoctorsForSelect();
}

function openModal(type, editing = false) {
    currentType = type;
    isEditing = editing;
    document.getElementById("modal").classList.remove("hidden");
}

function closeModal() {
    document.getElementById("modal").classList.add("hidden");
}

function modalSave() {
    if (isEditing) {
        submitFormEdit();
    } else {
        submitForm();
    }
}

async function submitForm() {
    let payload = {};

    if (currentType === "doctor") {
        payload = {
            doctor_code: document.getElementById("new_doctor_code").value,
            name: document.getElementById("new_doctor_name").value,
            email: document.getElementById("new_doctor_email").value,
            password: document.getElementById("new_doctor_password").value,
            id_clinic: document.getElementById("new_doctor_clinic").value || null
        };

        try {
            const res = await fetch(`${API}/admin/new/doctor`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (res.ok) {
                alert("Doctor successfully created!");
                closeModal();
                loadAdminDoctors();
            } else {
                alert("Error creating doctor: " + JSON.stringify(data));
            }
        } catch (err) {
            console.error(err);
            alert("Server connection error.");
        }

    } else if (currentType === "patient") {
        payload = {
            dni: document.getElementById("new_patient_dni").value,
            name: document.getElementById("new_patient_name").value,
            email: document.getElementById("new_patient_email").value,
            password: document.getElementById("new_patient_password").value,
            id_doctor: document.getElementById("new_patient_doctor").value || null
        };

        try {
            const res = await fetch(`${API}/admin/new/patient`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (res.ok) {
                alert("Patient created successfully!");
                closeModal();
                loadAdminPatients();
            } else {
                alert("Error creating patient: " + JSON.stringify(data));
            }
        } catch (err) {
            console.error(err);
            alert("Server connection error.");
        }
    }
}

async function submitFormEdit() {
    let payload = {};
    let endpoint = "";
    let method = "PUT";

    if (currentType === "doctor") {
        const idDoctor = document.getElementById("edit_doctor_id").value;
        if (!idDoctor) return alert("The doctor's ID was not found.");

        payload = {
            name: document.getElementById("edit_doctor_name").value,
            email: document.getElementById("edit_doctor_email").value,
            id_clinic: document.getElementById("edit_doctor_clinic").value || null
        };

        const password = document.getElementById("edit_doctor_password").value;
        if (password) payload.password = password;

        endpoint = `${API}/admin/doctor/${idDoctor}`;

    } else if (currentType === "patient") {
        const idPatient = document.getElementById("edit_patient_id").value;
        if (!idPatient) return alert("Patient ID not found.");

        payload = {
            name: document.getElementById("edit_patient_name").value,
            email: document.getElementById("edit_patient_email").value,
            dni: document.getElementById("edit_patient_dni").value,
            id_doctor: document.getElementById("edit_patient_doctor").value || null
        };

        const password = document.getElementById("edit_patient_password").value;
        if (password) payload.password = password;

        endpoint = `${API}/admin/patient/${idPatient}`;
    }

    try {
        const res = await fetch(endpoint, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (res.ok) {
            alert(`${currentType === "doctor" ? "Doctor" : "Patiente"} updated successfully!`);
            closeModal();
            loadAdminDoctors();
            loadAdminPatients();
        } else {
            alert("Update error: " + JSON.stringify(data));
        }

    } catch (err) {
        console.error(err);
        showToast("Server connection error", "error");
    }
}

async function editDoctor(id_doctor) {
    try {
        const res = await fetch(`${API}/admin/doctor/${id_doctor}`);
        if (!res.ok) throw new Error("Doctor not found");
        const doctor = await res.json();

        document.getElementById("modal-title").innerText = "Edit Doctor";
        document.getElementById("modal-body").innerHTML = `
            <input type="hidden" id="edit_doctor_id" value="${doctor.id_doctor}">
            <input id="edit_doctor_code" placeholder="Doctor Code" value="${doctor.doctor_code}">
            <input id="edit_doctor_name" placeholder="Full Name" value="${doctor.name}">
            <input id="edit_doctor_email" placeholder="Email" value="${doctor.email}">
            <input id="edit_doctor_password" type="password" placeholder="Password (leave blank)">
            <input id="edit_doctor_clinic" placeholder="Clinic ID" value="${doctor.id_clinic || ''}">
        `;

        currentType = "doctor";
        isEditing = true;
        openModal("doctor", true);
        scrollToBottom();
    } catch (err) {
        console.error(err);
        alert("Error retrieving doctor's data");
    }
}

async function editPatient(id_patient) {
    try {
        const res = await fetch(`${API}/admin/patient/${id_patient}`);
        if (!res.ok) throw new Error("Patient not found");
        const patient = await res.json();

        document.getElementById("modal-title").innerText = "Edit Patient";
        document.getElementById("modal-body").innerHTML = `
            <input type="hidden" id="edit_patient_id" value="${patient.id_patient}">
            <input id="edit_patient_dni" placeholder="ID card / National Identity Card" value="${patient.dni}">
            <input id="edit_patient_name" placeholder="Full Name" value="${patient.name}">
            <input id="edit_patient_email" placeholder="Email" value="${patient.email}">
            <input id="edit_patient_password" type="password" placeholder="Password (leave blank)">
            <select id="edit_patient_doctor">
                <option value="">No doctor assigned</option>
            </select>
        `;

        const doctorRes = await fetch(`${API}/admin/doctors`);
        const doctors = await doctorRes.json();
        const select = document.getElementById("edit_patient_doctor");
        doctors.forEach(d => {
            const option = document.createElement("option");
            option.value = d.id_doctor;
            option.textContent = `${d.name} (${d.email})`;
            if (d.id_doctor === patient.id_doctor) option.selected = true;
            select.appendChild(option);
        });

        currentType = "patient";
        isEditing = true;
        openModal("patient", true);
        scrollToBottom();

    } catch (err) {
        console.error(err);
        alert("Error obtaining patient data");
    }
}

async function loadDoctorsForSelect() {
    try {
        const res = await fetch(`${API}/admin/doctors`);
        const doctors = await res.json();
        const select = document.getElementById("new_patient_doctor");
        if (!select) return;

        doctors.forEach(d => {
            const option = document.createElement("option");
            option.value = d.id_doctor;
            option.textContent = `${d.name} (${d.email})`;
            select.appendChild(option);
        });
    } catch (err) {
        console.error(err);
        alert("Error loading doctors");
    }
}