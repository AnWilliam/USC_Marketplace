var profileForm = document.getElementById('profileForm');
var profileSummary = document.getElementById('profileSummary');
var profilePhotoInput = document.getElementById('profilePhotoInput');
var profilePhotoPreview = document.getElementById('profilePhotoPreview');
var actionBtn = document.getElementById('profileActionBtn');
var bioDisplay = document.getElementById('bioDisplay');
var bioInput = document.getElementById('bioInput');
var editControls = document.getElementById('editControls');

var isEditing = false;

function setViewMode() {
    isEditing = false;

    actionBtn.textContent = 'Edit profile';

    bioDisplay.classList.remove('hidden');
    bioInput.classList.add('hidden');
    editControls.classList.add('hidden');

    bioDisplay.textContent = bioInput.value || 'No bio yet.';
}

function setEditMode() {
    isEditing = true;

    actionBtn.textContent = 'Save profile';

    bioDisplay.classList.add('hidden');
    bioInput.classList.remove('hidden');
    editControls.classList.remove('hidden');
}

function showProfilePhotoPreview(src) {
    if (!profilePhotoPreview) return;

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

        var bioVal = user.bio || '';
        profileForm.bio.value = bioVal;
        if (bioInput) bioInput.value = bioVal;
        if (bioDisplay) bioDisplay.textContent = bioVal || 'No bio yet.';

        var picField = profileForm.querySelector('[name="profilePicture"]');
        if (picField) {
            picField.value = user.profilePicture || '';
        }

        showProfilePhotoPreview(user.profilePicture || '');

        if (typeof window.refreshSiteNavAuth === 'function') {
            window.refreshSiteNavAuth();
        }

        setViewMode(); 
    });
}

if (profilePhotoInput && profilePhotoPreview) {
    profilePhotoInput.addEventListener('change', function() {
        var file = profilePhotoInput.files && profilePhotoInput.files[0];
        if (!file) return;

        var reader = new FileReader();
        reader.onload = function() {
            showProfilePhotoPreview(reader.result);
        };
        reader.readAsDataURL(file);
    });
}

if (actionBtn) {
    actionBtn.addEventListener('click', function () {
        if (!isEditing) {
            setEditMode();
        } else {
            profileForm.requestSubmit();
        }
    });
}

if (profileForm) {
    profileForm.addEventListener('submit', function(event) {
        event.preventDefault();

        var fd = new FormData();
        fd.append('bio', bioInput.value || '');

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

                if (bioInput) {
                    bioInput.value = data.data.bio || '';
                }
                if (bioDisplay) {
                    bioDisplay.textContent = data.data.bio || 'No bio yet.';
                }

                if (typeof window.refreshSiteNavAuth === 'function') {
                    window.refreshSiteNavAuth();
                }

                setViewMode(); 
            }
        });
    });
}

loadProfile();
