async function handleLogin() {
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const email = emailInput.value.trim();
    const password = passwordInput.value.trim();

    // Validar inputs
    const emailValid = validateInput(emailInput, { required: true });
    const passwordValid = validateInput(passwordInput, { required: true, minLength: 4 });

    if (!emailValid || !passwordValid) {
        showToast("Please complete all fields correctly", "error");
        return;
    }

    const loginBtn = document.getElementById('loginBtn');
    loginBtn.disabled = true;
    loginBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Starting...';

    try {
        const res = await fetch(`${API}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });

        const data = await res.json();

        if (!data.success) {
            showToast("Incorrect credentials", "error");
            loginBtn.disabled = false;
            loginBtn.innerHTML = 'Login';
            return;
        }

        showToast("Welcome!", "info");

        document.getElementById("loginPage").classList.add("hidden");
        document.getElementById("landingPage").classList.add("hidden");

        const role = data.role.toLowerCase();

        if (role === "patient" || role === "patients") {
            updateHistory('patient');
            document.getElementById("patientDashboard").classList.remove("hidden");
            document.getElementById("p_name").innerText = data.user.name;
            localStorage.setItem("patient_id", data.user.id);
            localStorage.setItem("token", data.token);
            loadPatientHistory();
            loadPatientAppointments();
        } else if (role === "doctor" || role === "doctors") {
            updateHistory('doctor');
            document.getElementById("doctorDashboard").classList.remove("hidden");
            document.getElementById("d_name").innerText = data.user.name;
            localStorage.setItem("doctor_id", data.user.id);
            localStorage.setItem("token", data.token);
            loadDoctorPatients();
            loadDoctorPending();
            loadDoctorAppointments();
        } else if (role === "admin") {
            updateHistory('admin');
            document.getElementById("adminDashboard").classList.remove("hidden");
            loadAdminDoctors();
            loadAdminPatients();
        }

        setTimeout(initScrollAnimations, 100);
        loginBtn.disabled = false;
        loginBtn.innerHTML = 'Login';

    } catch (error) {
        console.error(error);
        showToast("Error de conexión", "error");
        loginBtn.disabled = false;
        loginBtn.innerHTML = 'Login';
    }
}

function logout() {
    // Detener speech si está activo
    stopSpeech();

    // Limpiar la foto seleccionada
    deleteSelectedPhoto();

    // Volver al login en lugar de landing
    document.getElementById('patientDashboard').classList.add('hidden');
    document.getElementById('doctorDashboard').classList.add('hidden');
    document.getElementById('adminDashboard').classList.add('hidden');
    document.getElementById('landingPage').classList.add('hidden');
    document.getElementById('loginPage').classList.remove('hidden');

    // Limpiar datos
    localStorage.clear();

    // Scroll top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}