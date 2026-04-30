var profileForm = document.getElementById('profileForm');
var profileSummary = document.getElementById('profileSummary');
var profilePhotoInput = document.getElementById('profilePhotoInput');
var profilePhotoPreview = document.getElementById('profilePhotoPreview');

function showProfilePhotoPreview(src) {
    if (!profilePhotoPreview) {
        return;
    }
    if (!src || String(src).trim() === '') {
        profilePhotoPreview.removeAttribute('src');
        profilePhotoPreview.classList.remove('visible');
        return;
    }
    profilePhotoPreview.src = src;
    profilePhotoPreview.classList.add('visible');
}

function loadProfile() {
    apiGet('profile').then(function(data) {
        if (!data.success) {
            window.location.href = loginUrlWithNext();
            return;
        }
        var user = data.data;
        profileSummary.textContent = user.name + ' (' + user.email + ')';
        profileForm.bio.value = user.bio || '';
        var picField = profileForm.querySelector('[name="profilePicture"]');
        if (picField) {
            picField.value = user.profilePicture || '';
        }
        showProfilePhotoPreview(user.profilePicture || '');
        if (typeof window.refreshSiteNavAuth === 'function') {
            window.refreshSiteNavAuth();
        }
    });
}

if (profilePhotoInput && profilePhotoPreview) {
    profilePhotoInput.addEventListener('change', function() {
        var file = profilePhotoInput.files && profilePhotoInput.files[0];
        if (!file) {
            return;
        }
        var reader = new FileReader();
        reader.onload = function() {
            showProfilePhotoPreview(reader.result);
        };
        reader.readAsDataURL(file);
    });
}

if (profileForm) {
    profileForm.addEventListener('submit', function(event) {
        event.preventDefault();
        var fd = new FormData();
        fd.append('bio', profileForm.bio.value || '');
        var picUrlField = profileForm.querySelector('[name="profilePicture"]');
        if (picUrlField && picUrlField.value.trim() !== '') {
            fd.append('profilePicture', picUrlField.value.trim());
        }
        if (profilePhotoInput && profilePhotoInput.files && profilePhotoInput.files[0]) {
            var f = profilePhotoInput.files[0];
            fd.append('photo', f, f.name || 'profile.jpg');
        }
        apiPost('profile', fd).then(function(data) {
            showResult(data.message, !data.success);
            if (data.success && data.data) {
                showProfilePhotoPreview(data.data.profilePicture || '');
                if (picUrlField) {
                    picUrlField.value = data.data.profilePicture || '';
                }
                if (profilePhotoInput) {
                    profilePhotoInput.value = '';
                }
                if (typeof window.refreshSiteNavAuth === 'function') {
                    window.refreshSiteNavAuth();
                }
            }
        });
    });
}

loadProfile();
