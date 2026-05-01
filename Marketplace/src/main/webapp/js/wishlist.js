var wishlistGrid = document.getElementById('wishlistGrid');
var wishlistEmpty = document.getElementById('wishlistEmpty');

function renderWishlistCard(item) {
    var thumb = item.imageUrl || item.photo_path || 'images/usc-trojan-placeholder.svg';

    return ''
        + '<article class="item-card item-card-owner-wrap">'
        + '<div class="item-card-list-owner-actions">'
        + '<button type="button" class="btn-remove-listing-list" data-remove-id="' + item.itemID + '" title="Remove" aria-label="Remove">&times;</button>'
        + '</div>'
        + '<div class="item-card-thumb"><img src="' + thumb + '" alt=""></div>'
        + '<h2>' + escapeHtml(item.title) + '</h2>'
        + '<p class="price">' + money(item.price) + '</p>'
        + '<a class="button" href="item-detail.html?id=' + item.itemID + '">View item</a>'
        + '</article>';
}
function loadWishlist() {
    apiGet('wishlist').then(function(data) {
        if (!data.success) {
            showResult(data.message, true);
            return;
        }

        var items = data.data || [];

        if (items.length === 0) {
            wishlistEmpty.classList.remove('hidden');
            wishlistGrid.innerHTML = '';
            return;
        }

        wishlistEmpty.classList.add('hidden');
        wishlistGrid.innerHTML = items.map(renderWishlistCard).join('');
    });
}
wishlistGrid.addEventListener('click', function(e) {
    var btn = e.target.closest('[data-remove-id]');
    if (!btn) return;

    var itemID = btn.getAttribute('data-remove-id');

    fetch('wishlist?itemID=' + encodeURIComponent(itemID), {
        method: 'DELETE'
    })
    .then(res => res.json())
    .then(data => {
        if (!data.success) {
            showResult(data.message, true);
            return;
        }
        loadWishlist(); // refresh list
    });
});

loadWishlist();