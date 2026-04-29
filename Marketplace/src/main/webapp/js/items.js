var itemsList = document.getElementById('itemsList');
var itemDetail = document.getElementById('itemDetail');
var itemForm = document.getElementById('itemForm');
var searchForm = document.getElementById('searchForm');

function loadItems(url) {
    if (!itemsList) {
        return;
    }
    apiGet(url || 'items').then(function(data) {
        if (!data.success) {
            showResult(data.message, true);
            return;
        }
        var html = data.data.map(renderItemCard).join('');
        itemsList.innerHTML = html || '<p>No available items yet.</p>';
    });
}

function renderItemCard(item) {
    return ''
        + '<article class="item-card">'
        + '<h2>' + escapeHtml(item.title) + '</h2>'
        + '<p class="price">' + money(item.price) + '</p>'
        + '<p>' + escapeHtml(item.description || '') + '</p>'
        + '<a class="button" href="item-detail.html?id=' + item.itemID + '">View item</a>'
        + '</article>';
}

function loadItemDetail() {
    if (!itemDetail) {
        return;
    }
    var id = getQueryParam('id');
    apiGet('items?id=' + encodeURIComponent(id)).then(function(data) {
        if (!data.success) {
            showResult(data.message, true);
            return;
        }
        var item = data.data;
        itemDetail.innerHTML = ''
            + '<h1>' + escapeHtml(item.title) + '</h1>'
            + '<p class="price">' + money(item.price) + '</p>'
            + '<p>' + escapeHtml(item.description || '') + '</p>'
            + '<p>Status: ' + escapeHtml(item.status) + '</p>'
            + '<button id="contactSeller" class="button primary">Contact seller</button>';

        document.getElementById('contactSeller').addEventListener('click', function() {
            var formData = new FormData();
            formData.append('itemID', item.itemID);
            apiPost('conversations', formData).then(function(result) {
                showResult(result.message, !result.success);
                if (result.success) {
                    window.location.href = 'messages.html?conversationID=' + result.data.conversationID;
                }
            });
        });
    });
}

if (itemForm) {
    itemForm.addEventListener('submit', function(event) {
        event.preventDefault();
        apiPost('items', new FormData(itemForm)).then(function(data) {
            showResult(data.message, !data.success);
            if (data.success) {
                window.location.href = 'item-detail.html?id=' + data.data.itemID;
            }
        });
    });
}

if (searchForm) {
    searchForm.addEventListener('submit', function(event) {
        event.preventDefault();
        var params = new URLSearchParams(new FormData(searchForm));
        var query = params.toString();
        loadItems(query ? 'search?' + query : 'items');
    });
}

loadItems();
loadItemDetail();
