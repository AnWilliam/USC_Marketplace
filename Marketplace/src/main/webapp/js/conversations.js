var conversationList = document.getElementById('conversationList');

conversationList.addEventListener('click', function(e) {
    var card = e.target.closest('.conversation-card');
    if (!card) return;

    var id = card.getAttribute('data-id');
    window.location.href = 'messages.html?conversationID=' + id;
});

function loadConversations() {
    if (!conversationList) {
        return;
    }

    apiGet('conversations').then(function(data) {
        if (!data.success) {
            window.location.href = loginUrlWithNext();
            return;
        }

        var html = data.data.map(function(conversation) {
            var unreadBadge = '';

            if (conversation.unreadCount > 0) {
                unreadBadge = '<span class="unread-badge">'
                    + conversation.unreadCount
                    + ' unread</span>';
            }

            var itemTitle = conversation.itemTitle || 'Item';
            var otherUserName = conversation.otherUserName || 'User';
            var lastMessage = conversation.lastMessage
                ? escapeHtml(conversation.lastMessage)
                : 'No messages yet.';

            var avatarSrc = conversation.otherUserAvatarUrl && String(conversation.otherUserAvatarUrl).trim() !== ''
                ? escapeHtml(String(conversation.otherUserAvatarUrl).trim())
                : 'images/icon-profile.svg';


				return ''
				    + '<div class="conversation-card" data-id="' + conversation.conversationID + '">'

				    + '<div class="conversation-header">'
				    + '<strong>' + escapeHtml(itemTitle) + '</strong>'
				    + '</div>'

				    + '<div class="conversation-row">'
				    + '<img class="msg-avatar" src="' + avatarSrc + '" alt="">'

				    + '<div class="conv-row-body">'

				
				    + '<div class="conversation-user">' + escapeHtml(otherUserName) + '</div>'

				    // 🔹 message clearly separate
				    + '<div class="conversation-message">' + lastMessage + '</div>'

				    + unreadBadge

				    + '</div></div></div>';
        }).join('');

        conversationList.innerHTML = html || '<p>No conversations yet.</p>';
    });
}

loadConversations();
