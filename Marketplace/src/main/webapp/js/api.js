function apiGet(url) {
    return fetch(url, { credentials: 'include' }).then(function(response) {
        return response.json();
    });
}

function apiPost(url, formData) {
    var body = formData;
    if (formData instanceof FormData) {
        var hasFile = false;
        formData.forEach(function(value) {
            if (value instanceof File && value.size > 0) {
                hasFile = true;
            }
        });

        if (!hasFile) {
            body = new URLSearchParams();
            formData.forEach(function(value, key) {
                body.append(key, value);
            });
        }
    }

    return fetch(url, {
        method: 'POST',
        body: body,
        credentials: 'include'
    }).then(function(response) {
        return response.json();
    });
}

function showResult(message, isError) {
    var result = document.getElementById('result');
    if (!result) {
        return;
    }
    result.textContent = message || '';
    result.className = isError ? 'result error' : 'result';
}

function getQueryParam(name) {
    return new URLSearchParams(window.location.search).get(name);
}

function money(value) {
    return Number(value).toLocaleString(undefined, {
        style: 'currency',
        currency: 'USD'
    });
}

function escapeHtml(value) {
    var replacements = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;'
    };
    return String(value).replace(/[&<>"]/g, function(character) {
        return replacements[character];
    });
}
