function apiGet(url) {
    return fetch(url, { credentials: 'include' }).then(function(response) {
        return response.json();
    });
}

function apiPost(url, body) {
    var opts = {
        method: 'POST',
        credentials: 'include'
    };
    if (body instanceof FormData) {
        opts.body = body;
    } else if (body instanceof URLSearchParams) {
        opts.headers = { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' };
        opts.body = body;
    } else {
        opts.body = body;
    }
    return fetch(url, opts).then(function(response) {
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
