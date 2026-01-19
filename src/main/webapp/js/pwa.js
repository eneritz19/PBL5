// PWA INSTALL
window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredPrompt = e;
    document.getElementById('installPWA').style.display = 'flex';
});

document.getElementById('installPWA').addEventListener('click', async () => {
    if (!deferredPrompt) return;

    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;

    if (outcome === 'accepted') {
        showToast('¡App installed correctly!', 'success');
    }

    deferredPrompt = null;
    document.getElementById('installPWA').style.display = 'none';
});