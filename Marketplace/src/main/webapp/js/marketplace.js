(function() {
    var CATEGORY_SIDEBAR_ORDER = [
        'Clothing', 'Accessories', 'Electronics', 'Sports', 'Hobby', 'School Supplies', 'Entertainment'
    ];

    var SORT_OPTIONS = [
        { id: 'newest', label: 'Newest' },
        { id: 'priceAsc', label: 'Price: Low to High' },
        { id: 'priceDesc', label: 'Price: High to Low' }
    ];

    var FILTER_TAGS = [
        { id: 'Clothing', kind: 'cat' },
        { id: 'Electronics', kind: 'cat' },
        { id: 'Sports', kind: 'cat' },
        { id: 'Hobby', kind: 'cat' },
        { id: 'School Supplies', kind: 'cat' },
        { id: 'under25', kind: 'cap', cap: 25 },
        { id: 'under50', kind: 'cap', cap: 50 },
        { id: 'under100', kind: 'cap', cap: 100 }
    ];

    var state = {
        sourceItems: [],
        activeSort: 'newest',
        categoryFilter: null,
        underCap: null,
        searchDebounce: null
    };

    var listingsPage = document.getElementById('listings-page');
    var addPage = document.getElementById('add-listing-page');
    var itemsGrid = document.getElementById('itemsGrid');
    var sortPillsEl = document.getElementById('sortPills');
    var filterPillsEl = document.getElementById('filterPills');
    var searchInput = document.getElementById('sidebarSearch');
    var priceAbove = document.getElementById('priceAbove');
    var priceBelow = document.getElementById('priceBelow');
    var catLinkContainer = document.getElementById('categoryLinks');
    var categorySelect = document.getElementById('categorySelect');
    var listingForm = document.getElementById('listingForm');
    var btnCreate = document.getElementById('btnCreateListing');
    var btnBack = document.getElementById('btnBackToListings');
    var marketplaceResult = document.getElementById('marketplaceResult');
    var photoInput = document.getElementById('photoInput');
    var photoPreview = document.getElementById('photoPreview');
    var photoDrop = document.getElementById('photoDrop');

    function normalizeItem(raw) {
        var ts = 0;
        if (raw.dateListed) {
            ts = Date.parse(String(raw.dateListed).replace(' ', 'T'));
            if (isNaN(ts)) {
                ts = 0;
            }
        }
        return {
            id: raw.itemID,
            name: raw.title,
            price: parseFloat(raw.price),
            category: raw.categoryName || '',
            categoryID: raw.categoryID,
            seller: raw.sellerName || ('User ' + raw.sellerID),
            image: raw.imageUrl || null,
            condition: raw.itemCondition || '',
            description: raw.description || '',
            dateListed: ts
        };
    }

    function sortCategories(cats) {
        var map = {};
        cats.forEach(function(c) {
            map[c.categoryName] = c;
        });
        var ordered = [];
        CATEGORY_SIDEBAR_ORDER.forEach(function(name) {
            if (map[name]) {
                ordered.push(map[name]);
                delete map[name];
            }
        });
        Object.keys(map).sort().forEach(function(k) {
            ordered.push(map[k]);
        });
        return ordered;
    }

    function loadCategories() {
        return apiGet('categories').then(function(data) {
            if (!data.success || !data.data) {
                return;
            }
            var sorted = sortCategories(data.data);
            if (catLinkContainer) {
                catLinkContainer.innerHTML = '';
                sorted.forEach(function(c) {
                    var b = document.createElement('button');
                    b.type = 'button';
                    b.textContent = c.categoryName;
                    b.dataset.categoryName = c.categoryName;
                    b.addEventListener('click', function() {
                        catLinkContainer.querySelectorAll('button').forEach(function(x) {
                            x.classList.remove('active');
                        });
                        if (state.categoryFilter === c.categoryName) {
                            state.categoryFilter = null;
                        } else {
                            state.categoryFilter = c.categoryName;
                            b.classList.add('active');
                        }
                        syncFilterPillsWithState();
                        renderGrid();
                    });
                    catLinkContainer.appendChild(b);
                });
            }
            if (categorySelect) {
                categorySelect.innerHTML = '<option value="">Select a category</option>';
                data.data.forEach(function(c) {
                    var opt = document.createElement('option');
                    opt.value = String(c.categoryID);
                    opt.textContent = c.categoryName;
                    categorySelect.appendChild(opt);
                });
            }
        });
    }

    function fetchListings(url) {
        return apiGet(url || 'items').then(function(data) {
            if (!data.success) {
                showMsg(data.message || 'Could not load items.', true);
                return;
            }
            state.sourceItems = (data.data || []).map(normalizeItem);
            renderGrid();
        });
    }

    function applyClientFilters(items) {
        var out = items.slice();
        if (state.categoryFilter) {
            out = out.filter(function(i) {
                return i.category === state.categoryFilter;
            });
        }
        if (state.underCap != null) {
            out = out.filter(function(i) {
                return i.price < state.underCap;
            });
        }
        var lo = priceAbove && priceAbove.value.trim() !== '' ? parseFloat(priceAbove.value) : null;
        var hi = priceBelow && priceBelow.value.trim() !== '' ? parseFloat(priceBelow.value) : null;
        if (lo != null && !isNaN(lo)) {
            out = out.filter(function(i) {
                return i.price >= lo;
            });
        }
        if (hi != null && !isNaN(hi)) {
            out = out.filter(function(i) {
                return i.price <= hi;
            });
        }
        return out;
    }

    function sortItems(items) {
        var arr = items.slice();
        if (state.activeSort === 'priceAsc') {
            arr.sort(function(a, b) {
                return a.price - b.price;
            });
        } else if (state.activeSort === 'priceDesc') {
            arr.sort(function(a, b) {
                return b.price - a.price;
            });
        } else {
            arr.sort(function(a, b) {
                return b.dateListed - a.dateListed;
            });
        }
        return arr;
    }

    function renderGrid() {
        if (!itemsGrid) {
            return;
        }
        var filtered = applyClientFilters(state.sourceItems);
        var sorted = sortItems(filtered);
        if (!sorted.length) {
            itemsGrid.innerHTML = '<p class="empty-state">No listings match your filters.</p>';
            return;
        }
        itemsGrid.innerHTML = sorted.map(renderCard).join('');
    }

    function renderCard(item) {
        var imgHtml = item.image
            ? '<div class="card-img-placeholder"><img src="' + escapeHtml(item.image) + '" alt=""></div>'
            : '<div class="card-img-placeholder">No image</div>';
        return ''
            + '<article class="item-card-market">'
            + '<a href="item-detail.html?id=' + item.id + '">'
            + imgHtml
            + '<div class="card-body-market">'
            + '<p class="card-price">' + money(item.price) + '</p>'
            + '<h3 class="card-title">' + escapeHtml(item.name) + '</h3>'
            + '<p class="card-seller">' + escapeHtml(item.seller) + '</p>'
            + '</div></a></article>';
    }

    function showMsg(text, isError) {
        var addVisible = addPage && !addPage.classList.contains('page-hidden');
        var el = addVisible ? document.getElementById('listingFormMsg') : marketplaceResult;
        if (!el) {
            return;
        }
        el.textContent = text || '';
        el.className = 'result-banner' + (isError ? ' error' : text ? ' success' : '');
    }

    function buildPills() {
        if (sortPillsEl) {
            sortPillsEl.innerHTML = SORT_OPTIONS.map(function(opt) {
                var active = state.activeSort === opt.id ? ' active' : '';
                return '<button type="button" class="pill sort-pill' + active + '" data-sort="' + opt.id + '">' + escapeHtml(opt.label) + '</button>';
            }).join('');
            sortPillsEl.querySelectorAll('.sort-pill').forEach(function(btn) {
                btn.addEventListener('click', function() {
                    state.activeSort = btn.getAttribute('data-sort');
                    sortPillsEl.querySelectorAll('.sort-pill').forEach(function(b) {
                        b.classList.toggle('active', b === btn);
                    });
                    renderGrid();
                });
            });
        }

        if (filterPillsEl) {
            filterPillsEl.innerHTML = FILTER_TAGS.map(function(t) {
                var active = false;
                if (t.kind === 'cat') {
                    active = state.categoryFilter === t.id;
                } else if (t.kind === 'cap') {
                    active = state.underCap === t.cap;
                }
                return '<button type="button" class="pill filter-pill' + (active ? ' active' : '') + '" data-kind="' + t.kind + '" data-id="' + escapeHtml(t.id) + '"' + (t.cap != null ? ' data-cap="' + t.cap + '"' : '') + '>' + escapeHtml(t.id.indexOf('under') === 0 ? ('Under $' + t.cap) : t.id) + '</button>';
            }).join('');

            filterPillsEl.querySelectorAll('.filter-pill').forEach(function(btn) {
                btn.addEventListener('click', function() {
                    var kind = btn.getAttribute('data-kind');
                    if (kind === 'cat') {
                        var name = btn.getAttribute('data-id');
                        if (state.categoryFilter === name) {
                            state.categoryFilter = null;
                        } else {
                            state.categoryFilter = name;
                        }
                        if (catLinkContainer) {
                            catLinkContainer.querySelectorAll('button').forEach(function(b) {
                                b.classList.toggle('active', b.dataset.categoryName === state.categoryFilter);
                            });
                        }
                    } else {
                        var cap = parseInt(btn.getAttribute('data-cap'), 10);
                        if (state.underCap === cap) {
                            state.underCap = null;
                        } else {
                            state.underCap = cap;
                        }
                    }
                    syncFilterPillsWithState();
                    renderGrid();
                });
            });
        }
    }

    function syncFilterPillsWithState() {
        if (!filterPillsEl) {
            return;
        }
        filterPillsEl.querySelectorAll('.filter-pill').forEach(function(btn) {
            var kind = btn.getAttribute('data-kind');
            var on = false;
            if (kind === 'cat') {
                on = state.categoryFilter === btn.getAttribute('data-id');
            } else {
                on = state.underCap === parseInt(btn.getAttribute('data-cap'), 10);
            }
            btn.classList.toggle('active', on);
        });
    }

    function showView(which) {
        if (which === 'add') {
            if (listingsPage) {
                listingsPage.classList.add('page-hidden');
            }
            if (addPage) {
                addPage.classList.remove('page-hidden');
            }
        } else {
            if (listingsPage) {
                listingsPage.classList.remove('page-hidden');
            }
            if (addPage) {
                addPage.classList.add('page-hidden');
            }
        }
    }

    if (searchInput) {
        searchInput.addEventListener('input', function() {
            clearTimeout(state.searchDebounce);
            state.searchDebounce = setTimeout(function() {
                var q = searchInput.value.trim();
                if (q) {
                    fetchListings('search?' + new URLSearchParams({ q: q }).toString());
                } else {
                    fetchListings('items');
                }
            }, 350);
        });
    }

    var filterLink = document.getElementById('searchFiltersLink');
    if (filterLink && searchInput) {
        filterLink.addEventListener('click', function() {
            searchInput.focus();
            searchInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
        });
    }

    if (priceAbove) {
        priceAbove.addEventListener('input', function() {
            renderGrid();
        });
    }
    if (priceBelow) {
        priceBelow.addEventListener('input', function() {
            renderGrid();
        });
    }

    if (btnCreate) {
        btnCreate.addEventListener('click', function() {
            window.location.hash = 'create';
            showView('add');
            showMsg('', false);
        });
    }
    if (btnBack) {
        btnBack.addEventListener('click', function() {
            window.location.hash = '';
            showView('listings');
        });
    }

    if (photoInput && photoPreview) {
        photoInput.addEventListener('change', function() {
            var file = photoInput.files && photoInput.files[0];
            if (!file) {
                photoPreview.classList.remove('visible');
                photoPreview.removeAttribute('src');
                return;
            }
            var reader = new FileReader();
            reader.onload = function() {
                photoPreview.src = reader.result;
                photoPreview.classList.add('visible');
            };
            reader.readAsDataURL(file);
        });
    }
    if (photoDrop && photoInput) {
        photoDrop.addEventListener('dragover', function(e) {
            e.preventDefault();
        });
        photoDrop.addEventListener('drop', function(e) {
            e.preventDefault();
            if (e.dataTransfer.files && e.dataTransfer.files[0]) {
                photoInput.files = e.dataTransfer.files;
                photoInput.dispatchEvent(new Event('change'));
            }
        });
    }

    if (listingForm) {
        listingForm.addEventListener('submit', function(event) {
            event.preventDefault();
            var title = listingForm.title.value.trim();
            var priceRaw = listingForm.price.value.trim();
            var categoryID = listingForm.categoryID.value;
            var desc = listingForm.description.value.trim();
            var condInput = listingForm.querySelector('input[name="itemCondition"]:checked');
            var itemCondition = condInput ? condInput.value : '';

            if (!title || !priceRaw || !categoryID) {
                showMsg('Please fill in item name, price, and category.', true);
                return;
            }

            var fd = new FormData();
            fd.append('title', title);
            fd.append('price', priceRaw);
            fd.append('categoryID', categoryID);
            fd.append('description', desc);
            fd.append('itemCondition', itemCondition);
            if (photoPreview && photoPreview.src) {
                fd.append('imageUrl', photoPreview.src);
            }

            apiPost('items', fd).then(function(data) {
                if (!data.success) {
                    showMsg(data.message || 'Could not create listing.', true);
                    return;
                }
                showMsg('Listing posted.', false);
                listingForm.reset();
                if (photoPreview) {
                    photoPreview.classList.remove('visible');
                    photoPreview.removeAttribute('src');
                }
                showView('listings');
                var q = searchInput && searchInput.value.trim();
                fetchListings(q ? 'search?' + new URLSearchParams({ q: q }).toString() : 'items');
            });
        });
    }

    loadCategories().then(function() {
        buildPills();
        return fetchListings('items');
    }).then(function() {
        if (window.location.hash === '#create') {
            showView('add');
        }
    });

    window.addEventListener('hashchange', function() {
        if (window.location.hash === '#create') {
            showView('add');
        } else {
            showView('listings');
        }
    });
})();