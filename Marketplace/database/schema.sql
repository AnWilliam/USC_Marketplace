CREATE DATABASE IF NOT EXISTS usc_marketplace;
USE usc_marketplace;

CREATE TABLE IF NOT EXISTS Users (
    userID INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    profile_picture VARCHAR(500),
    bio TEXT
);

CREATE TABLE IF NOT EXISTS Categories (
    categoryID INT AUTO_INCREMENT PRIMARY KEY,
    categoryName VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS Items (
    itemID INT AUTO_INCREMENT PRIMARY KEY,
    sellerID INT NOT NULL,
    categoryID INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    item_condition VARCHAR(32) NULL,
    photo_path VARCHAR(500) NULL,
    price DECIMAL(10, 2) NOT NULL,
    status ENUM('AVAILABLE', 'SOLD', 'PENDING', 'WITHDRAWN') NOT NULL DEFAULT 'AVAILABLE',
    date_listed TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_seller FOREIGN KEY (sellerID) REFERENCES Users(userID) ON DELETE CASCADE,
    CONSTRAINT fk_items_category FOREIGN KEY (categoryID) REFERENCES Categories(categoryID),
    CONSTRAINT chk_items_price CHECK (price > 0),
    INDEX idx_items_status (status),
    INDEX idx_items_title (title)
);

CREATE TABLE IF NOT EXISTS Conversations (
    conversationID INT AUTO_INCREMENT PRIMARY KEY,
    itemID INT NOT NULL,
    buyerID INT NOT NULL,
    sellerID INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP NULL,
    CONSTRAINT fk_conversations_item FOREIGN KEY (itemID) REFERENCES Items(itemID) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_buyer FOREIGN KEY (buyerID) REFERENCES Users(userID) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_seller FOREIGN KEY (sellerID) REFERENCES Users(userID) ON DELETE CASCADE,
    CONSTRAINT uq_conversation UNIQUE (itemID, buyerID, sellerID),
    INDEX idx_conversations_user (buyerID, sellerID)
);

CREATE TABLE IF NOT EXISTS Messages (
    messageID INT AUTO_INCREMENT PRIMARY KEY,
    conversationID INT NOT NULL,
    senderID INT NOT NULL,
    content TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversationID) REFERENCES Conversations(conversationID) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (senderID) REFERENCES Users(userID) ON DELETE CASCADE,
    INDEX idx_messages_conversation (conversationID, timestamp)
);

CREATE TABLE IF NOT EXISTS Wishlist (
    wishlistID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    itemID INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wishlist_user FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_item FOREIGN KEY (itemID) REFERENCES Items(itemID) ON DELETE CASCADE,
    CONSTRAINT uq_wishlist UNIQUE (userID, itemID),
    INDEX idx_wishlist_user (userID),
    INDEX idx_wishlist_item (itemID)
);

INSERT IGNORE INTO Categories (categoryName, description) VALUES
('Textbooks', 'Books, course readers, and study materials'),
('Electronics', 'Laptops, chargers, calculators, and accessories'),
('Furniture', 'Dorm and apartment furniture'),
('Clothing', 'Clothes, shoes, and USC gear'),
('Other', 'Everything else'),
('Accessories', 'Bags, jewelry, tech accessories'),
('Sports', 'Athletic gear and equipment'),
('Hobby', 'Games, crafts, and collectibles'),
('School Supplies', 'Notebooks, pens, desk organizers'),
('Entertainment', 'Movies, music, tickets, and media');
