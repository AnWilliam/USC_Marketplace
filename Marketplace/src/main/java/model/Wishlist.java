package model;

import java.time.LocalDateTime;

public class Wishlist {
    private int wishlistID;
    private int userID;
    private int itemID;
    private LocalDateTime createdAt;

    public Wishlist() {
    }

    public Wishlist(int wishlistID, int userID, int itemID, LocalDateTime createdAt) {
        this.wishlistID = wishlistID;
        this.userID = userID;
        this.itemID = itemID;
        this.createdAt = createdAt;
    }

    public int getWishlistID() {
        return wishlistID;
    }

    public void setWishlistID(int wishlistID) {
        this.wishlistID = wishlistID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Wishlist{" +
                "wishlistID=" + wishlistID +
                ", userID=" + userID +
                ", itemID=" + itemID +
                ", createdAt=" + createdAt +
                '}';
    }
}
