var itemsList = document.getElementById('itemsList');
var itemDetail = document.getElementById('itemDetail');
var itemForm = document.getElementById('itemForm');
var searchForm = document.getElementById('searchForm');
var imageInput = document.getElementById('imageInput');
var imagePreview = document.getElementById('imagePreview');

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
    var imageHtml = '';
    if (item.imageUrls && item.imageUrls.length > 0) {
        imageHtml = '<img class="item-thumb" src="' + escapeHtml(item.imageUrls[0]) + '" alt="' + escapeHtml(item.title) + '">';
    }
    return ''
        + '<article class="item-card">'
        + imageHtml
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
        var galleryHtml = renderGallery(item);
        itemDetail.innerHTML = ''
            + '<h1>' + escapeHtml(item.title) + '</h1>'
            + galleryHtml
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

function renderGallery(item) {
    if (!item.imageUrls || item.imageUrls.length === 0) {
        return '';
    }
    var html = '<div class="item-gallery">';
    for (var i = 0; i < item.imageUrls.length; i++) {
        html += '<img src="' + escapeHtml(item.imageUrls[i]) + '" alt="' + escapeHtml(item.title) + ' photo ' + (i + 1) + '">';
    }
    html += '</div>';
    return html;
}

if (itemForm) {
    itemForm.addEventListener('submit', function(event) {
        event.preventDefault();
        if (imageInput && imageInput.files.length > 6) {
            showResult('You can upload up to 6 images.', true);
            return;
        }
        apiPost('items', new FormData(itemForm)).then(function(data) {
            showResult(data.message, !data.success);
            if (data.success) {
                window.location.href = 'item-detail.html?id=' + data.data.itemID;
            }
        });
    });
}

if (imageInput) {
    imageInput.addEventListener('change', function() {
        if (!imagePreview) {
            return;
        }
        imagePreview.innerHTML = '';
        if (imageInput.files.length > 6) {
            showResult('You can upload up to 6 images.', true);
            imageInput.value = '';
            return;
        }
        for (var i = 0; i < imageInput.files.length; i++) {
            var img = document.createElement('img');
            img.src = URL.createObjectURL(imageInput.files[i]);
            img.alt = 'Selected item photo';
            imagePreview.appendChild(img);
        }
        showResult('');
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
