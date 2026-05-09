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

// Helper functions for popup context
function escapeHtml(str) {
    return String(str).replace(/[&<>'"]/g, function (c) {
        return {'&':'&amp;','<':'&lt;','>':'&gt;','\'':'&#39;','"':'&quot;'}[c];
    });
}
function showResult(msg, isError) {
    var result = document.getElementById('result');
    if (result) {
        result.textContent = msg || '';
        result.className = 'result' + (isError ? ' error' : '');
    }
}
function loginUrlWithNext() {
    return 'login.html?next=' + encodeURIComponent(window.location.pathname + window.location.search);
}

// Conversation logic for popup
var popupConversationList = document.getElementById('popupConversationList');
var currentConversationID = null;

function loadPopupConversations() {
    if (!popupConversationList) return;
    apiGet('conversations').then(function(data) {
        if (!data.success) {
            popupConversationList.innerHTML = '<p style="padding:8px;">Login required</p>';
            return;
        }
        var html = data.data.map(function(conversation) {
            var unreadBadge = conversation.unreadCount > 0 ? '<span class="unread-badge">' + conversation.unreadCount + '</span>' : '';
            var itemTitle = conversation.itemTitle || 'Item';
            var otherUserName = conversation.otherUserName || 'User';
            var avatarSrc = conversation.otherUserAvatarUrl && String(conversation.otherUserAvatarUrl).trim() !== ''
                ? escapeHtml(String(conversation.otherUserAvatarUrl).trim())
                : 'images/icon-profile.svg';
            return '<div class="popup-conv-card" data-id="' + conversation.conversationID + '" style="padding:6px;cursor:pointer;display:flex;align-items:center;gap:4px;' + (currentConversationID==conversation.conversationID?'background:#ffe':'') + '">' +
                '<img src="' + avatarSrc + '" style="width:28px;height:28px;border-radius:50%;">' +
                '<div style="flex:1;overflow:hidden;">' +
                '<div style="font-size:12px;font-weight:bold;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">' + escapeHtml(itemTitle) + '</div>' +
                '<div style="font-size:11px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">' + escapeHtml(otherUserName) + '</div>' +
                '</div>' + unreadBadge + '</div>';
        }).join('');
        popupConversationList.innerHTML = html || '<p style="padding:8px;">No conversations</p>';
    });
}

popupConversationList && popupConversationList.addEventListener('click', function(e) {
    var card = e.target.closest('.popup-conv-card');
    if (!card) return;
    var id = card.getAttribute('data-id');
    if (id) {
        currentConversationID = id;
        loadPopupMessages();
        loadPopupConversations();
    }
});

function loadPopupMessages() {
    var messageList = document.getElementById('messageList');
    var messageForm = document.getElementById('messageForm');
    if (!messageList || !currentConversationID) {
        messageList.innerHTML = '<p style="padding:8px;">Select a conversation</p>';
        if (messageForm) messageForm.style.display = 'none';
        return;
    }
    apiGet('messages?conversationID=' + encodeURIComponent(currentConversationID)).then(function(data) {
        if (!data.success) {
            showResult('Login required', true);
            messageList.innerHTML = '';
            if (messageForm) messageForm.style.display = 'none';
            return;
        }
        if (messageForm) messageForm.style.display = '';
        var html = data.data.map(function(message) {
            var receipt = message.read ? 'Read' : 'Delivered';
            var avatarSrc = message.senderAvatarUrl && String(message.senderAvatarUrl).trim() !== ''
                ? escapeHtml(String(message.senderAvatarUrl).trim())
                : 'images/icon-profile.svg';
            return '<div class="msg-row">' +
                '<img class="msg-avatar" src="' + avatarSrc + '" alt="">' +
                '<div>' +
                '<strong>' + escapeHtml(message.senderName || ('User #' + message.senderID)) + '</strong>' +
                '<p>' + escapeHtml(message.content) + '</p>' +
                '<small>' + escapeHtml(formatTime(message.timestamp)) + ' • ' + receipt + '</small>' +
                '</div></div>';
        }).join('');
        messageList.innerHTML = html || '<p>No messages yet.</p>';
        markPopupConversationAsRead();
    });
}

function markPopupConversationAsRead() {
    if (!currentConversationID) return;
    var formData = new FormData();
    formData.append('conversationID', currentConversationID);
    formData.append('action', 'markAsRead');
    apiPost('messages', formData);
}

var messageForm = document.getElementById('messageForm');
if (messageForm) {
    messageForm.addEventListener('submit', function(event) {
        event.preventDefault();
        if (!currentConversationID) return;
        apiGet('profile').then(function(sess) {
            if (!sess.success) {
                showResult('Login required', true);
                return;
            }
            var formData = new FormData(messageForm);
            formData.append('conversationID', currentConversationID);
            apiPost('messages', formData).then(function(data) {
                showResult(data.message, !data.success);
                if (data.success) {
                    messageForm.reset();
                    loadPopupMessages();
                }
            });
        });
    });
}

// Popup open/close logic
var chatPopup = document.getElementById('chatPopup');
var chatPopupOpen = document.getElementById('chatPopupOpen');
var chatPopupClose = document.getElementById('chatPopupClose');
if (chatPopupOpen) {
    chatPopupOpen.addEventListener('click', function() {
        chatPopup.classList.remove('closed');
        loadPopupConversations();
        loadPopupMessages();
    });
}
if (chatPopupClose) {
    chatPopupClose.addEventListener('click', function() {
        chatPopup.classList.add('closed');
    });
}
// Load conversations on popup open
if (chatPopup) {
    chatPopup.addEventListener('transitionend', function() {
        if (!chatPopup.classList.contains('closed')) {
            loadPopupConversations();
            loadPopupMessages();
        }
    });
}

loadMessages();