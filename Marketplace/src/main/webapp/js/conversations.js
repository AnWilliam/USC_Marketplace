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
            return ''
                + '<div class="list-row">'
                + '<strong>Item #' + conversation.itemID + '</strong>'
                + '<p>Buyer #' + conversation.buyerID + ' and seller #' + conversation.sellerID + '</p>'
                + '<a class="button" href="messages.html?conversationID=' + conversation.conversationID + '">Open</a>'
                + '</div>';
        }).join('');
        conversationList.innerHTML = html || '<p>No conversations yet.</p>';
    });
}

loadConversations();
