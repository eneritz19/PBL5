// FILTROS DE BÚSQUEDA
function filterDoctors(query) {
    const cards = document.querySelectorAll('#adminDoctorsList .case-card');
    cards.forEach(card => {
        const text = card.textContent.toLowerCase();
        card.style.display = text.includes(query.toLowerCase()) ? 'flex' : 'none';
    });
}

function filterAdminPatients(query) {
    const cards = document.querySelectorAll('#adminPatientsList .case-card');
    cards.forEach(card => {
        const text = card.textContent.toLowerCase();
        card.style.display = text.includes(query.toLowerCase()) ? 'flex' : 'none';
    });
}

function filterPatients(query) {
    const cards = document.querySelectorAll('#doctorPatientsList .case-card');
    cards.forEach(card => {
        const text = card.textContent.toLowerCase();
        card.style.display = text.includes(query.toLowerCase()) ? 'flex' : 'none';
    });
}