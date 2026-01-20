function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    const icons = {
        success: 'fa-circle-check',
        error: 'fa-circle-xmark',
        info: 'fa-circle-info',
        warning: 'fa-triangle-exclamation'
    };

    toast.innerHTML = `
                <i class="fa-solid ${icons[type]}"></i>
                <span>${message}</span>
            `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(400px)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function showLoading(text = 'Processing...') {
    const existing = document.getElementById('loadingSpinner');
    if (existing) return;

    const overlay = document.createElement('div');
    overlay.id = 'loadingSpinner';
    overlay.className = 'spinner-overlay';
    overlay.innerHTML = `
                <div class="spinner"></div>
                <div class="spinner-text">${text}</div>
            `;
    document.body.appendChild(overlay);
}

function hideLoading() {
    const spinner = document.getElementById('loadingSpinner');
    if (spinner) spinner.remove();
}

function showConfirm(title, message, icon = '⚠️', onConfirm) {
    const modal = document.createElement('div');
    modal.className = 'confirm-modal';
    modal.innerHTML = `
                <div class="confirm-content">
                    <div class="confirm-icon">${icon}</div>
                    <h3 class="confirm-title">${title}</h3>
                    <p class="confirm-message">${message}</p>
                    <div class="confirm-actions">
                        <button class="btn-secondary" onclick="this.closest('.confirm-modal').remove()">
                            Cancel
                        </button>
                        <button class="btn-primary" id="confirmBtn" style="background: #ef4444;">
                            Confirm
                        </button>
                    </div>
                </div>
            `;

    document.body.appendChild(modal);

    document.getElementById('confirmBtn').onclick = () => {
        onConfirm();
        modal.remove();
    };
}

function toggleDarkMode() {
    document.body.classList.toggle('dark-mode');

    const isDark = document.body.classList.contains('dark-mode');
    localStorage.setItem('darkMode', isDark);

    const icons = ['darkModeIcon', 'darkModeIconDoctor', 'darkModeIconAdmin'];
    const texts = ['darkModeText', 'darkModeTextDoctor', 'darkModeTextAdmin'];

    icons.forEach(id => {
        const icon = document.getElementById(id);
        if (icon) {
            icon.className = isDark ? 'fa-solid fa-sun' : 'fa-solid fa-moon';
        }
    });

    texts.forEach(id => {
        const text = document.getElementById(id);
        if (text) {
            text.textContent = isDark ? 'Light Mode' : 'Dark Mode';
        }
    });

    showToast(isDark ? 'Dark mode activated' : 'Light mode activated', 'info');
}

window.addEventListener('DOMContentLoaded', () => {
    const savedDarkMode = localStorage.getItem('darkMode');
    if (savedDarkMode === 'true') {
        document.body.classList.add('dark-mode');
        const icons = ['darkModeIcon', 'darkModeIconDoctor', 'darkModeIconAdmin'];
        const texts = ['darkModeText', 'darkModeTextDoctor', 'darkModeTextAdmin'];

        icons.forEach(id => {
            const icon = document.getElementById(id);
            if (icon) icon.className = 'fa-solid fa-sun';
        });

        texts.forEach(id => {
            const text = document.getElementById(id);
            if (text) text.textContent = 'Modo Claro';
        });
    }
});

const animateOnScroll = () => {
    const elements = document.querySelectorAll('.animate-on-scroll, .animate-scale');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' });
    elements.forEach(el => observer.observe(el));
};

const observerOptions = { root: null, rootMargin: '0px', threshold: 0.1 };

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            observer.unobserve(entry.target);
        }
    });
}, observerOptions);

function initScrollAnimations() {
    const elements = document.querySelectorAll('.case-card, .container, .tabs, .reveal-on-scroll');
    elements.forEach(el => observer.observe(el));
}


function scrollToBottom() {
    setTimeout(() => {
        window.scrollTo({ top: document.body.scrollHeight, behavior: "smooth" });
    }, 150);
}

function validateInput(input, rules) {
    const value = input.value.trim();
    const errorDiv = document.getElementById(input.id + 'Error');

    if (rules.required && !value) {
        input.classList.add('error');
        input.classList.remove('success');
        if (errorDiv) {
            errorDiv.style.display = 'flex';
            errorDiv.querySelector('span').textContent = 'This field is required';
        }
        return false;
    }

    if (rules.email && value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        input.classList.add('error');
        input.classList.remove('success');
        if (errorDiv) {
            errorDiv.style.display = 'flex';
            errorDiv.querySelector('span').textContent = 'Invalid email';
        }
        return false;
    }

    if (rules.minLength && value.length < rules.minLength) {
        input.classList.add('error');
        input.classList.remove('success');
        if (errorDiv) {
            errorDiv.style.display = 'flex';
            errorDiv.querySelector('span').textContent = `Minimum ${rules.minLength} characters`;
        }
        return false;
    }

    input.classList.remove('error');
    input.classList.add('success');
    if (errorDiv) errorDiv.style.display = 'none';
    return true;
}

function updateCharCount(textarea) {
    const maxLength = textarea.getAttribute('maxlength') || 500;
    const current = textarea.value.length;
    const counterId = textarea.id + '-count';
    let counter = document.getElementById(counterId);

    if (!counter) {
        counter = document.createElement('div');
        counter.id = counterId;
        counter.className = 'char-counter';
        textarea.after(counter);
    }

    counter.textContent = `${current}/${maxLength}`;

    if (current > maxLength * 0.9) {
        counter.classList.add('danger');
        counter.classList.remove('warning');
    } else if (current > maxLength * 0.7) {
        counter.classList.add('warning');
        counter.classList.remove('danger');
    } else {
        counter.classList.remove('warning', 'danger');
    }
}

function openImageModal(imageSrc) {
    const modal = document.getElementById('imageModal');
    const modalImg = document.getElementById('modalImage');
    modal.classList.remove('hidden');
    modalImg.src = imageSrc;
}

function closeImageModal() {
    document.getElementById('imageModal').classList.add('hidden');
}

function openTab(tabId, btnElement) {
    const activeDashboard = document.querySelector('.container:not(.hidden)');
    if (activeDashboard) {
        activeDashboard.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
        activeDashboard.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
        const targetContent = activeDashboard.querySelector(`#${tabId}`);
        if (targetContent) targetContent.classList.add('active');
        if (btnElement) btnElement.classList.add('active');
    }
    setTimeout(initScrollAnimations, 50);
}