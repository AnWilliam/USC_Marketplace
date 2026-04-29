var conversationList = document.getElementById('conversationList');

function loadConversations() {
    if (!conversationList) {
        return;
    }

    apiGet('conversations').then(function(data) {
        if (!data.success) {
            showResult(data.message, true);
            return;
        }

        var html = data.data.map(function(conversation) {
            var unreadBadge = '';

            if (conversation.unreadCount > 0) {
                unreadBadge = '<span class="unread-badge">'
                    + conversation.unreadCount
                    + ' unread</span>';
            }

            var lastMessage = conversation.lastMessage
                ? escapeHtml(conversation.lastMessage)
                : 'No messages yet.';

            return ''
                + '<div class="list-row">'
                + '<strong>' + escapeHtml(conversation.itemTitle) + '</strong>'
                + '<p>With ' + escapeHtml(conversation.otherUserName) + '</p>'
                + '<p>' + lastMessage + '</p>'
                + unreadBadge
                + '<br>'
                + '<a class="button" href="messages.html?conversationID='
                + conversation.conversationID
                + '">Open</a>'
                + '</div>';
        }).join('');

        conversationList.innerHTML = html || '<p>No conversations yet.</p>';
    });
}

loadConversations();