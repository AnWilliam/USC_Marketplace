function parseFetchResponse(response) {
    return response.text().then(function(text) {
        var trimmed = text.trim();
        if (!trimmed) {
            return {
                success: false,
                message: response.ok ? 'Empty server response.' : ('Request failed (HTTP ' + response.status + ').')
            };
        }
        var ch = trimmed.charAt(0);
        if (ch === '{' || ch === '[') {
            try {
                return JSON.parse(trimmed);
            } catch (parseErr) {
                var snippet = trimmed.slice(0, 160).replace(/<[^>]+>/g, ' ');
                return {
                    success: false,
                    message: response.ok
                        ? ('Server returned invalid JSON: ' + snippet)
                        : ('HTTP ' + response.status + ': ' + snippet)
                };
            }
        }
        var stripped = trimmed.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();
        return {
            success: false,
            message: stripped.slice(0, 280) || ('Request failed (HTTP ' + response.status + ').')
        };
    });
}

function apiGet(url) {
    return fetch(url, { credentials: 'include' }).then(parseFetchResponse).catch(function(err) {
        return { success: false, message: err.message || 'Network error.' };
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
    return fetch(url, opts).then(parseFetchResponse).catch(function(err) {
        return { success: false, message: err.message || 'Network error.' };
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

/** Relative URL back to current HTML page + query (for login ?next=...). */
function loginUrlWithNext() {
    var path = window.location.pathname || '';
    var slash = path.lastIndexOf('/');
    var leaf = slash >= 0 ? path.slice(slash + 1) : path;
    var qs = window.location.search || '';
    return 'login.html?next=' + encodeURIComponent(leaf + qs);
}

/** Safe redirect target after login/register from ?next= (same-folder relative only). */
function consumeRedirectAfterLogin(fallbackUrl) {
    var raw = getQueryParam('next');
    if (!raw) {
        return fallbackUrl || 'items.html';
    }
    try {
        var decoded = decodeURIComponent(raw);
        if (decoded.indexOf('://') !== -1 || decoded.indexOf('..') !== -1 || decoded.charAt(0) === '/') {
            return fallbackUrl || 'items.html';
        }
        if (/^[a-zA-Z0-9_.\-/?=&%]+$/.test(decoded)) {
            return decoded;
        }
    } catch (e) {
        /* ignore */
    }
    return fallbackUrl || 'items.html';
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
