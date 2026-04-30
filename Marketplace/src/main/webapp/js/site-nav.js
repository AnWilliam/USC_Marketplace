(function() {
    var loginRegisterSkip = /\/(?:login|register)\.html$/i.test(window.location.pathname || '');

    function bindLogout() {
        var logoutBtn = document.getElementById('siteNavLogout');
        if (!logoutBtn) {
            return;
        }
        logoutBtn.addEventListener('click', function() {
            apiPost('logout', new FormData()).then(function(data) {
                if (data.success) {
                    window.location.href = 'login.html';
                }
            });
        });
    }

    function refreshNavAuthState() {
        if (loginRegisterSkip) {
            document.body.classList.remove('auth-unknown');
            document.body.classList.add('auth-login-register');
            return;
        }
        document.body.classList.add('auth-unknown');
        apiGet('profile').then(function(data) {
            document.body.classList.remove('auth-unknown');
            document.body.classList.add(data.success ? 'auth-user' : 'auth-guest');
            var pic = document.getElementById('navProfileThumb');
            if (pic && data.success && data.data && data.data.profilePicture) {
                var url = String(data.data.profilePicture).trim();
                if (url) {
                    pic.src = url;
                    pic.classList.add('nav-profile-thumb-filled');
                }
            }
        }).catch(function() {
            document.body.classList.remove('auth-unknown');
            document.body.classList.add('auth-guest');
        });
    }

    document.addEventListener('DOMContentLoaded', function() {
        bindLogout();
        refreshNavAuthState();
    });

    window.refreshSiteNavAuth = refreshNavAuthState;
})();
