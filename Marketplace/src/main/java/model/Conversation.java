package model;

import java.time.LocalDateTime;

public class Conversation {
    private int conversationID;
    private int itemID;
    private int buyerID;
    private int sellerID;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    public Conversation() {
    }

    public Conversation(int conversationID, int itemID, int buyerID, int sellerID, LocalDateTime createdAt, LocalDateTime lastMessageAt) {
        this.conversationID = conversationID;
        this.itemID = itemID;
        this.buyerID = buyerID;
        this.sellerID = sellerID;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
    }

    public int getConversationID() {
        return conversationID;
    }

    public void setConversationID(int conversationID) {
        this.conversationID = conversationID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getBuyerID() {
        return buyerID;
    }

    public void setBuyerID(int buyerID) {
        this.buyerID = buyerID;
    }

    public int getSellerID() {
        return sellerID;
    }

    public void setSellerID(int sellerID) {
        this.sellerID = sellerID;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}