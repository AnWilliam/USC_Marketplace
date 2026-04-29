USE usc_marketplace;

CREATE TABLE IF NOT EXISTS ItemImages (
    imageID INT AUTO_INCREMENT PRIMARY KEY,
    itemID INT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 1,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_images_item FOREIGN KEY (itemID) REFERENCES Items(itemID) ON DELETE CASCADE,
    INDEX idx_item_images_item (itemID, display_order)
);
