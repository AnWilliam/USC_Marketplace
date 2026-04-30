var messageList = document.getElementById('messageList');
var messageForm = document.getElementById('messageForm');
var conversationID = getQueryParam('conversationID');

var PLACEHOLDER_AVATAR = 'images/icon-profile.svg';

function formatTime(timestamp) {
    if (!timestamp) return '';

    timestamp = timestamp.replace('Z', '');

    const parts = timestamp.split(/[T ]/);
    if (parts.length < 2) return '';

    const [year, month, day] = parts[0].split('-').map(Number);
    const [hour, minute, second] = parts[1].split(':').map(Number);

    const date = new Date(year, month - 1, day, hour, minute, second || 0);

    return date.toLocaleTimeString([], {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
    });
}

function setMessagingGuestMode(on) {
    if (!messageForm) {
        return;
    }
    messageForm.style.display = on ? 'none' : '';
}

function loadMessages() {
    if (!messageList || !conversationID) {
        return;
    }
    apiGet('messages?conversationID=' + encodeURIComponent(conversationID)).then(function(data) {
        if (!data.success) {
            window.location.href = loginUrlWithNext();
            setMessagingGuestMode(true);
            return;
        }
        setMessagingGuestMode(false);
        var html = data.data.map(function(message) {
            var receipt = message.read ? 'Read' : 'Delivered';

            var avatarSrc = message.senderAvatarUrl && String(message.senderAvatarUrl).trim() !== ''
                ? escapeHtml(String(message.senderAvatarUrl).trim())
                : PLACEHOLDER_AVATAR;

            return ''
                + '<div class="msg-row">'
                + '<img class="msg-avatar" src="' + avatarSrc + '" alt="">'
                + '<div>'
                + '<strong>' + escapeHtml(message.senderName || ('User #' + message.senderID)) + '</strong>'
                + '<p>' + escapeHtml(message.content) + '</p>'
                + '<small>' + escapeHtml(formatTime(message.timestamp)) + ' • ' + receipt + '</small>'
                + '</div>'
                + '</div>';
        }).join('');
        messageList.innerHTML = html || '<p>No messages yet.</p>';
        markConversationAsRead();
    });
}

function markConversationAsRead() {
    if (!conversationID) {
        return;
    }

    var formData = new FormData();
    formData.append('conversationID', conversationID);
    formData.append('action', 'markAsRead');

    apiPost('messages', formData).then(function(data) {
        if (!data.success) {
            console.error(data.message || 'Could not mark messages as read.');
        }
    });
}

if (messageForm) {
    messageForm.addEventListener('submit', function(event) {
        event.preventDefault();
        if (!conversationID) {
            return;
        }
        apiGet('profile').then(function(sess) {
            if (!sess.success) {
                window.location.href = loginUrlWithNext();
                return;
            }
            var formData = new FormData(messageForm);
            formData.append('conversationID', conversationID);
            apiPost('messages', formData).then(function(data) {
                showResult(data.message, !data.success);
                if (data.success) {
                    messageForm.reset();
                    loadMessages();
                }
            });
        });
    });
}

loadMessages();
