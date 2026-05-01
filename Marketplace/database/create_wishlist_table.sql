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
