var messageList = document.getElementById('messageList');
var messageForm = document.getElementById('messageForm');
var conversationID = getQueryParam('conversationID');

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
                + '<strong>User #' + message.senderID + '</strong>'
                + '<p>' + escapeHtml(message.content) + '</p>'
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
