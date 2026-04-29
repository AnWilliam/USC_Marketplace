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
    price DECIMAL(10, 2) NOT NULL,
    status ENUM('AVAILABLE', 'SOLD', 'PENDING') NOT NULL DEFAULT 'AVAILABLE',
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
