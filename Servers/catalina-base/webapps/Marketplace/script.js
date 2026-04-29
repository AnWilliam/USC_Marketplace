// --- USC Marketplace Listings Data and State ---
let listings = [
  {
    id: 1,
    name: "USC Hoodie",
    price: 35,
    category: "Clothing",
    seller: "Tommy Trojan",
    image: "",
    condition: "Like New",
    description: "Official USC hoodie, barely worn. Size M."
  },
  {
    id: 2,
    name: "TI-84 Calculator",
    price: 60,
    category: "School Supplies",
    seller: "Jane Smith",
    image: "",
    condition: "Good",
    description: "Works perfectly, includes cover."
  },
  {
    id: 3,
    name: "Sony Headphones",
    price: 45,
    category: "Electronics",
    seller: "Alex Lee",
    image: "",
    condition: "Fair",
    description: "Wireless, battery lasts 10 hours."
  },
  {
    id: 4,
    name: "USC Scarf",
    price: 15,
    category: "Accessories",
    seller: "Chris Kim",
    image: "",
    condition: "New",
    description: "Brand new, never used."
  },
  {
    id: 5,
    name: "Basketball",
    price: 20,
    category: "Sports",
    seller: "Jordan P.",
    image: "",
    condition: "Good",
    description: "Official size, lightly used."
  },
  {
    id: 6,
    name: "Guitar",
    price: 90,
    category: "Hobby",
    seller: "Sam R.",
    image: "",
    condition: "Like New",
    description: "Acoustic, great sound."
  },
  {
    id: 7,
    name: "Movie DVDs Bundle",
    price: 18,
    category: "Entertainment",
    seller: "Priya S.",
    image: "",
    condition: "Good",
    description: "5 popular movies."
  },
  {
    id: 8,
    name: "USC Cap",
    price: 12,
    category: "Clothing",
    seller: "Tommy Trojan",
    image: "",
    condition: "Fair",
    description: "Classic USC cap, faded color."
  }
];
let filteredListings = [...listings];
let currentSort = "newest";
let currentCategory = null;
let currentPriceAbove = null;
let currentPriceBelow = null;
let currentFilterTags = [];
let searchTerm = "";

// --- DOM Elements ---
const listingsPage = document.getElementById('listings-page');
const addListingPage = document.getElementById('add-listing-page');
const listingsGrid = document.getElementById('listings-grid');
const createListingBtn = document.getElementById('create-listing-btn');
const backToListings = document.getElementById('back-to-listings');
const addListingForm = document.getElementById('add-listing-form');
const sortingTags = document.getElementById('sorting-tags');
const searchBar = document.getElementById('search-bar');
const priceAbove = document.getElementById('price-above');
const priceBelow = document.getElementById('price-below');
const categoryLinks = document.querySelectorAll('.category-link');

// --- Utility Functions ---
function renderListings() {
  let arr = [...listings];

  // Search
  if (searchTerm) {
    arr = arr.filter(l => l.name.toLowerCase().includes(searchTerm.toLowerCase()) || l.description.toLowerCase().includes(searchTerm.toLowerCase()));
  }

  // Category filter (sidebar or tag)
  if (currentCategory) {
    arr = arr.filter(l => l.category === currentCategory);
  }

  // Price filter
  if (currentPriceAbove !== null && currentPriceAbove !== "") {
    arr = arr.filter(l => l.price >= Number(currentPriceAbove));
  }
  if (currentPriceBelow !== null && currentPriceBelow !== "") {
    arr = arr.filter(l => l.price <= Number(currentPriceBelow));
  }

  // Tag filters (e.g. under $25, $50, $100)
  currentFilterTags.forEach(tag => {
    if (tag === "under25") arr = arr.filter(l => l.price < 25);
    if (tag === "under50") arr = arr.filter(l => l.price < 50);
    if (tag === "under100") arr = arr.filter(l => l.price < 100);
    if (["Clothing","Electronics","Sports","Hobby","School Supplies"].includes(tag)) arr = arr.filter(l => l.category === tag);
  });

  // Sorting
  if (currentSort === "low") {
    arr.sort((a, b) => a.price - b.price);
  } else if (currentSort === "high") {
    arr.sort((a, b) => b.price - a.price);
  } else {
    arr.sort((a, b) => b.id - a.id); // Newest first
  }

  filteredListings = arr;
  listingsGrid.innerHTML = arr.length ? arr.map(cardHTML).join('') : '<div style="grid-column: 1/-1; text-align:center; color:#990000;">No listings found.</div>';
}

function cardHTML(l) {
  return `<div class="card">
    ${l.image ? `<img src="${l.image}" class="card-image" alt="${l.name}">` : `<div class="card-placeholder">No Image</div>`}
    <div class="card-price">$${l.price}</div>
    <div class="card-title">${l.name}</div>
    <div class="card-seller">${l.seller}</div>
    <div class="card-condition">${l.condition}</div>
  </div>`;
}

function resetFilters() {
  currentCategory = null;
  currentPriceAbove = null;
  currentPriceBelow = null;
  currentFilterTags = [];
  searchTerm = "";
  searchBar.value = "";
  priceAbove.value = "";
  priceBelow.value = "";
  document.querySelectorAll('.tag-pill').forEach(btn => btn.classList.remove('active'));
  document.querySelector('.tag-pill[data-sort="newest"]').classList.add('active');
}

// --- Event Listeners ---
createListingBtn.onclick = () => {
  listingsPage.style.display = 'none';
  addListingPage.style.display = 'block';
};
backToListings.onclick = (e) => {
  e.preventDefault();
  addListingPage.style.display = 'none';
  listingsPage.style.display = 'block';
  addListingForm.reset();
  document.getElementById('image-preview').style.display = 'none';
};

// Sorting/Filter Tags
sortingTags.addEventListener('click', function(e) {
  if (e.target.classList.contains('tag-pill')) {
    const sort = e.target.getAttribute('data-sort');
    const filter = e.target.getAttribute('data-filter');
    if (sort) {
      document.querySelectorAll('.tag-pill[data-sort]').forEach(btn => btn.classList.remove('active'));
      e.target.classList.add('active');
      currentSort = sort;
    } else if (filter) {
      if (e.target.classList.contains('active')) {
        e.target.classList.remove('active');
        currentFilterTags = currentFilterTags.filter(f => f !== filter);
      } else {
        e.target.classList.add('active');
        currentFilterTags.push(filter);
      }
    }
    renderListings();
  }
});

// Sidebar Category Links
categoryLinks.forEach(link => {
  link.onclick = function(e) {
    e.preventDefault();
    currentCategory = this.getAttribute('data-category');
    document.querySelectorAll('.category-link').forEach(l => l.classList.remove('usc-red'));
    this.classList.add('usc-red');
    renderListings();
  };
});

// Sidebar Price Inputs
priceAbove.oninput = function() {
  currentPriceAbove = this.value;
  renderListings();
};
priceBelow.oninput = function() {
  currentPriceBelow = this.value;
  renderListings();
};

// Search Bar
searchBar.oninput = function() {
  searchTerm = this.value;
  renderListings();
};

// --- Add Listing Form Logic ---
const uploadBox = document.getElementById('upload-box');
const itemImageInput = document.getElementById('item-image');
const imagePreview = document.getElementById('image-preview');

uploadBox.onclick = () => itemImageInput.click();
uploadBox.ondragover = (e) => {
  e.preventDefault();
  uploadBox.classList.add('dragover');
};
uploadBox.ondragleave = () => uploadBox.classList.remove('dragover');
uploadBox.ondrop = (e) => {
  e.preventDefault();
  uploadBox.classList.remove('dragover');
  if (e.dataTransfer.files && e.dataTransfer.files[0]) {
    itemImageInput.files = e.dataTransfer.files;
    showImagePreview(e.dataTransfer.files[0]);
  }
};
itemImageInput.onchange = function() {
  if (this.files && this.files[0]) {
    showImagePreview(this.files[0]);
  }
};
function showImagePreview(file) {
  const reader = new FileReader();
  reader.onload = function(e) {
    imagePreview.src = e.target.result;
    imagePreview.style.display = 'block';
  };
  reader.readAsDataURL(file);
}

addListingForm.onsubmit = function(e) {
  e.preventDefault();
  // Validate required fields
  const name = document.getElementById('item-name').value.trim();
  const price = Number(document.getElementById('item-price').value);
  const category = document.getElementById('item-category').value;
  const description = document.getElementById('item-description').value.trim();
  const condition = document.querySelector('input[name="item-condition"]:checked');
  let image = "";
  if (itemImageInput.files && itemImageInput.files[0]) {
    image = imagePreview.src;
  }
  if (!name || !price || !category || !condition) {
    alert('Please fill out all required fields.');
    return;
  }
  const newListing = {
    id: Date.now(),
    name,
    price,
    category,
    seller: "You",
    image,
    condition: condition.value,
    description
  };
  listings.unshift(newListing);
  resetFilters();
  renderListings();
  addListingPage.style.display = 'none';
  listingsPage.style.display = 'block';
  addListingForm.reset();
  imagePreview.style.display = 'none';
};

// --- Initial Render ---
renderListings();
