var registerForm = document.getElementById('registerForm');
if (registerForm) {
    console.log('USC Marketplace auth.js v4 loaded');
    registerForm.addEventListener('submit', function(event) {
        event.preventDefault();
        registerForm.name.value = registerForm.name.value.trim();
        registerForm.email.value = registerForm.email.value.trim().toLowerCase();
        console.log('Submitting email:', registerForm.email.value);
        apiPost('register', new FormData(registerForm)).then(function(data) {
            showResult(data.message, !data.success);
            if (data.success) {
                window.location.href = consumeRedirectAfterLogin('items.html');
            }
        });
    });
}

var loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', function(event) {
        event.preventDefault();
        loginForm.email.value = loginForm.email.value.trim().toLowerCase();
        apiPost('login', new FormData(loginForm)).then(function(data) {
            showResult(data.message, !data.success);
            if (data.success) {
                window.location.href = consumeRedirectAfterLogin('items.html');
            }
        });
    });
}
