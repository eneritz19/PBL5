async function editAppointment(id_appointment, currentDate, patientId) {
    const modal = document.createElement('div');
    modal.className = 'confirm-modal';
    modal.innerHTML = `
                <div class="confirm-content">
                    <h3 class="confirm-title">Edit Appointment</h3>
                    <p class="confirm-message">Select the new date and time for the appointment</p>
                    <input type="datetime-local" id="editAppointmentDate" value="${currentDate.slice(0, 16)}" 
                           style="width: 100%; padding: 12px; border: 1px solid rgba(0,0,0,0.1); border-radius: 12px; 
                                  font-size: 16px; margin: 20px 0; background: #f5f5f7;">
                    <div class="confirm-actions">
                        <button class="btn-secondary" onclick="this.closest('.confirm-modal').remove()">
                            Cancel
                        </button>
                        <button class="btn-primary" id="confirmEditBtn">
                            Update
                        </button>
                    </div>
                </div>
            `;

    document.body.appendChild(modal);

    document.getElementById('confirmEditBtn').onclick = async () => {
        const newDate = document.getElementById('editAppointmentDate').value;
        if (!newDate) {
            showToast("Please select a date and time", "warning");
            return;
        }

        modal.remove();
        showLoading("Updating appointment...");

        try {
            const res = await fetch(`${API}/appointment/${id_appointment}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ date: newDate })
            });

            hideLoading();

            if (res.ok) {
                showToast("Appointment updated successfully", "success");
                await loadDoctorAppointments();
                await loadPatientAppointments();
            } else {
                const data = await res.json();
                showToast("Error updating appointment: " + JSON.stringify(data), "error");
            }
        } catch (err) {
            console.error(err);
            hideLoading();
            showToast("Connection error updating appointment", "error");
        }
    };
}

async function deleteAppointment(id_appointment, patientId) {
    showConfirm(
        "Delete Appointment",
        "Are you sure you want to delete this appointment and its diagnosis?",
        "",
        async () => {
            showLoading("Deleting...");
            try {
                const res = await fetch(`${API}/appointment/${id_appointment}`, {
                    method: "DELETE"
                });

                if (!res.ok) {
                    const data = await res.json();
                    throw new Error(JSON.stringify(data));
                }

                await loadDoctorAppointments();
                await loadPatientAppointments();
                await loadPatientHistory();

                hideLoading();
                showToast("Quote successfully removed", "success");

            } catch (err) {
                console.error(err);
                hideLoading();
                showToast("Error deleting quote", "error");
            }
        }
    );
}