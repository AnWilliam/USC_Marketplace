var profileForm = document.getElementById('profileForm');
var profileSummary = document.getElementById('profileSummary');
var logoutButton = document.getElementById('logoutButton');

function loadProfile() {
    apiGet('profile').then(function(data) {
        if (!data.success) {
            showResult(data.message, true);
            return;
        }
        var user = data.data;
        profileSummary.textContent = user.name + ' (' + user.email + ')';
        profileForm.bio.value = user.bio || '';
        profileForm.profilePicture.value = user.profilePicture || '';
    });
}

if (profileForm) {
    profileForm.addEventListener('submit', function(event) {
        event.preventDefault();
        apiPost('profile', new FormData(profileForm)).then(function(data) {
            showResult(data.message, !data.success);
        });
    });
}

if (logoutButton) {
    logoutButton.addEventListener('click', function() {
        apiPost('logout', new FormData()).then(function(data) {
            showResult(data.message, !data.success);
            if (data.success) {
                window.location.href = 'login.html';
            }
        });
    });
}

loadProfile();
