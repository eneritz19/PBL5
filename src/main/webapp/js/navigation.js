function updateHistory(view) {
    currentView = view;
    history.pushState({ view: view }, '', `#${view}`);
}

window.addEventListener('popstate', function (event) {
    if (event.state && event.state.view) {
        navigateToView(event.state.view);
    } else {
        navigateToView('landing');
    }
});

function navigateToView(view) {
    document.getElementById('landingPage').classList.add('hidden');
    document.getElementById('loginPage').classList.add('hidden');
    document.getElementById('patientDashboard').classList.add('hidden');
    document.getElementById('doctorDashboard').classList.add('hidden');
    document.getElementById('adminDashboard').classList.add('hidden');

    if (view === 'landing') document.getElementById('landingPage').classList.remove('hidden');
    else if (view === 'login') document.getElementById('loginPage').classList.remove('hidden');
    else if (view === 'patient') document.getElementById('patientDashboard').classList.remove('hidden');
    else if (view === 'doctor') document.getElementById('doctorDashboard').classList.remove('hidden');
    else if (view === 'admin') document.getElementById('adminDashboard').classList.remove('hidden');

    currentView = view;
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showLogin() {
    updateHistory('login');
    document.getElementById('landingPage').classList.add('hidden');
    document.getElementById('loginPage').classList.remove('hidden');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showLanding() {
    updateHistory('landing');
    document.getElementById('loginPage').classList.add('hidden');
    document.getElementById('landingPage').classList.remove('hidden');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function changeStep(step) {
    document.querySelectorAll('.step-animation').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.step-indicator').forEach(i => i.classList.remove('active'));
    document.getElementById('step' + step).classList.add('active');
    document.querySelectorAll('.step-indicator')[step - 1].classList.add('active');
    currentStep = step;
}

function autoChangeStep() {
    currentStep = currentStep >= 3 ? 1 : currentStep + 1;
    changeStep(currentStep);
}

