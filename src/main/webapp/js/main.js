window.addEventListener('load', () => {
    setTimeout(() => {
        document.getElementById('logoLoader').classList.add('hidden');
        document.getElementById('landingPage').classList.remove('hidden');
        animateOnScroll();
    }, 3000);
});

stepInterval = setInterval(autoChangeStep, 4000);