var ITEM_IMAGE_PLACEHOLDER = 'images/usc-trojan-placeholder.svg';

function itemStoredPhoto(item) {
    var u = item.imageUrl || item.photoUrl || item.photo_path;
    return u != null && String(u).trim() !== '' ? String(u).trim() : null;
}

var itemsList = document.getElementById('itemsList');
var itemDetail = document.getElementById('itemDetail');
var itemForm = document.getElementById('itemForm');
var searchForm = document.getElementById('searchForm');

var currentUserID = null;
var lastItemsUrl = 'items';

function refreshSessionUser() {
    return apiGet('profile').then(function(data) {
        currentUserID = data.success && data.data ? data.data.userID : null;
    }).catch(function() {
        currentUserID = null;
    });
}

function updateItemStatus(itemID, status) {
    var fd = new FormData();
    fd.append('action', 'updateStatus');
    fd.append('itemID', String(itemID));
    fd.append('status', status);
    return apiPost('items', fd);
}

function bindItemsListOwnerActions() {
    if (!itemsList || itemsList.dataset.ownerBound === '1') {
        return;
    }
    itemsList.dataset.ownerBound = '1';
    itemsList.addEventListener('click', function(event) {
        var btn = event.target.closest('[data-owner-action]');
        if (!btn) {
            return;
        }
        event.preventDefault();
        event.stopPropagation();
        var itemID = parseInt(btn.getAttribute('data-item-id'), 10);
        var action = btn.getAttribute('data-owner-action');
        if (action === 'withdraw' && !window.confirm('Remove this listing from the marketplace?')) {
            return;
        }
        var status = action === 'sold' ? 'SOLD' : 'WITHDRAWN';
        updateItemStatus(itemID, status).then(function(data) {
            showResult(data.message, !data.success);
            if (data.success) {
                loadItems(lastItemsUrl);
            }
        });
    });
}

function loadItems(url) {
    if (!itemsList) {
        return;
    }
    var u = url || 'items';
    lastItemsUrl = u;
    apiGet(u).then(function(data) {
        if (!data.success) {
            showResult(data.message, true);
            return;
        }
        var html = data.data.map(renderItemCard).join('');
        itemsList.innerHTML = html || '<p>No available items yet.</p>';
        bindItemsListOwnerActions();
    });
}

function renderItemCard(item) {
    var stored = itemStoredPhoto(item);
    var thumb = stored || ITEM_IMAGE_PLACEHOLDER;
    var thumbClass = 'item-card-thumb' + (stored ? ' item-card-thumb-photo' : '');
    var ownerBar = '';
    var wrapClass = 'item-card';
    if (currentUserID != null && Number(item.sellerID) === Number(currentUserID) && item.status === 'AVAILABLE') {
        wrapClass += ' item-card-owner-wrap';
        ownerBar = ''
            + '<div class="item-card-list-owner-actions">'
            + '<button type="button" class="btn-mark-sold-list" data-owner-action="sold" data-item-id="' + item.itemID + '">Mark sold</button>'
            + '<button type="button" class="btn-remove-listing-list" data-owner-action="withdraw" data-item-id="' + item.itemID + '" title="Remove listing" aria-label="Remove listing">&times;</button>'
            + '</div>';
    }
    return ''
        + '<article class="' + wrapClass + '">'
        + ownerBar
        + '<div class="' + thumbClass + '"><img src="' + escapeHtml(thumb) + '" alt=""></div>'
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
    if (!id) {
        return;
    }
    apiGet('items?id=' + encodeURIComponent(id)).then(function(data) {
        if (!data.success) {
            showResult(data.message, true);
            return;
        }
        var item = data.data;
        var storedHero = itemStoredPhoto(item);
        var hero = storedHero || ITEM_IMAGE_PLACEHOLDER;
        var heroFigureClass = 'item-detail-hero' + (storedHero ? ' item-detail-has-photo' : '');

        var notice = '';
        if (item.status !== 'AVAILABLE') {
            notice = '<p class="item-unavailable-notice">This listing is no longer available (status: ' + escapeHtml(item.status) + ').</p>';
        }

        var ownerBar = '';
        if (currentUserID != null && Number(item.sellerID) === Number(currentUserID) && item.status === 'AVAILABLE') {
            ownerBar = ''
                + '<div class="item-detail-owner-actions">'
                + '<button type="button" id="detailMarkSold" class="button">Mark sold</button>'
                + '<button type="button" id="detailRemoveListing" class="button btn-remove-detail" title="Remove listing" aria-label="Remove listing">&times;</button>'
                + '</div>';
        }

        var contactBtn = '';
        if (item.status === 'AVAILABLE' && (currentUserID == null || Number(item.sellerID) !== Number(currentUserID))) {
            contactBtn = '<button type="button" id="contactSeller" class="button primary">Contact seller</button>';
        }

        itemDetail.innerHTML = ''
            + '<figure class="' + heroFigureClass + '"><img src="' + escapeHtml(hero) + '" alt=""></figure>'
            + notice
            + '<h1>' + escapeHtml(item.title) + '</h1>'
            + '<p class="price">' + money(item.price) + '</p>'
            + '<p>' + escapeHtml(item.description || '') + '</p>'
            + '<p>Status: ' + escapeHtml(item.status) + '</p>'
            + ownerBar
            + contactBtn;

        var contactSellerEl = document.getElementById('contactSeller');
        if (contactSellerEl) {
            contactSellerEl.addEventListener('click', function() {
                if (currentUserID == null) {
                    window.location.href = loginUrlWithNext();
                    return;
                }
                var formData = new FormData();
                formData.append('itemID', item.itemID);
                apiPost('conversations', formData).then(function(result) {
                    if (!result.success && result.message && String(result.message).toLowerCase().indexOf('log in') !== -1) {
                        window.location.href = loginUrlWithNext();
                        return;
                    }
                    showResult(result.message, !result.success);
                    if (result.success) {
                        window.location.href = 'messages.html?conversationID=' + result.data.conversationID;
                    }
                });
            });
        }

        var markSoldEl = document.getElementById('detailMarkSold');
        if (markSoldEl) {
            markSoldEl.addEventListener('click', function() {
                updateItemStatus(item.itemID, 'SOLD').then(function(result) {
                    showResult(result.message, !result.success);
                    if (result.success) {
                        window.location.reload();
                    }
                });
            });
        }

        var removeEl = document.getElementById('detailRemoveListing');
        if (removeEl) {
            removeEl.addEventListener('click', function() {
                if (!window.confirm('Remove this listing from the marketplace?')) {
                    return;
                }
                updateItemStatus(item.itemID, 'WITHDRAWN').then(function(result) {
                    showResult(result.message, !result.success);
                    if (result.success) {
                        window.location.reload();
                    }
                });
            });
        }
    });
}

if (itemForm) {
    var itemFormSubmitting = false;
    itemForm.addEventListener('submit', function(event) {
        event.preventDefault();
        if (itemFormSubmitting) {
            return;
        }
        var publishBtn = itemForm.querySelector('button[type="submit"]');
        itemFormSubmitting = true;
        if (publishBtn) {
            publishBtn.disabled = true;
        }
        apiPost('items', new FormData(itemForm)).then(function(data) {
            showResult(data.message, !data.success);
            if (data.success) {
                window.location.href = 'item-detail.html?id=' + data.data.itemID;
            }
        }).finally(function() {
            itemFormSubmitting = false;
            if (publishBtn) {
                publishBtn.disabled = false;
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

refreshSessionUser().then(function() {
    loadItems();
    loadItemDetail();
});
