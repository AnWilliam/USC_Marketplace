package model;

import java.time.LocalDateTime;

public class ItemImage {
    private int imageID;
    private int itemID;
    private String imageUrl;
    private int displayOrder;
    private LocalDateTime uploadedAt;

    public ItemImage() {
    }

    public ItemImage(int imageID, int itemID, String imageUrl, int displayOrder, LocalDateTime uploadedAt) {
        this.imageID = imageID;
        this.itemID = itemID;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.uploadedAt = uploadedAt;
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
