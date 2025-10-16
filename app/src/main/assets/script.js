function createRipple(event) {
    const button = event.currentTarget;
    const circle = document.createElement("span");
    const diameter = Math.max(button.clientWidth, button.clientHeight);
    const radius = diameter / 2;

    circle.style.width = circle.style.height = `${diameter}px`;
    circle.style.left = `${event.clientX - button.offsetLeft - radius}px`;
    circle.style.top = `${event.clientY - button.offsetTop - radius}px`;
    circle.classList.add("ripple");

    const ripple = button.getElementsByClassName("ripple")[0];
    if (ripple) {
        ripple.remove();
    }

    button.appendChild(circle);
}

document.querySelectorAll('.btn-secondary').forEach(btn => {
    btn.addEventListener('click', createRipple);
});

let isEnabled = false;

function toggleNotifications() {
    const btn = document.getElementById('mainBtn');
    if (isEnabled) {
        if (window.AndroidInterface) {
            window.AndroidInterface.disableNotifications();
        }
        isEnabled = false;
        btn.textContent = 'Enable Daily Popups';
        btn.classList.remove('enabled');
    } else {
        if (window.AndroidInterface) {
            window.AndroidInterface.enableNotifications();
        }
    }
}

function testNow() {
    if (window.AndroidInterface) {
        window.AndroidInterface.testNow();
    }
}

function test30s() {
    if (window.AndroidInterface) {
        window.AndroidInterface.test30s();
    }
}

function notificationsEnabled() {
    const btn = document.getElementById('mainBtn');
    isEnabled = true;
    btn.textContent = 'Disable Daily Popups';
    btn.classList.add('enabled');
}