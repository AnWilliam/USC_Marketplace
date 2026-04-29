var messageList = document.getElementById('messageList');
var messageForm = document.getElementById('messageForm');
var conversationID = getQueryParam('conversationID');

// Helper to format timestamps into a readable time string
function formatTime(timestamp) {
    if (!timestamp) return "";

    // Remove any timezone indicators if present
    timestamp = timestamp.replace("Z", "");

    const parts = timestamp.split(/[T ]/);
    if (parts.length < 2) return "";

    const [year, month, day] = parts[0].split("-").map(Number);
    const [hour, minute, second] = parts[1].split(":").map(Number);

    // Force LOCAL time
    const date = new Date(year, month - 1, day, hour, minute, second || 0);

    return date.toLocaleTimeString([], {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
    });
}

function loadMessages() {
    if (!messageList || !conversationID) {
        return;
    }
    apiGet('messages?conversationID=' + encodeURIComponent(conversationID)).then(function(data) {
        if (!data.success) {
            showResult(data.message, true);
            return;
        }
        var html = data.data.map(function(message) {
            return ''
                + '<div class="message">'
                + '<strong>' + (message.senderName || ('User #' + message.senderID)) + '</strong>'
                + '<p>' + escapeHtml(message.content) + '</p>'
                + '<small>' + escapeHtml(formatTime(message.timestamp)) + '</small>'
                + '</div>';
        }).join('');
        messageList.innerHTML = html || '<p>No messages yet.</p>';
    });
}

if (messageForm) {
    messageForm.addEventListener('submit', function(event) {
        event.preventDefault();
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
}

loadMessages();