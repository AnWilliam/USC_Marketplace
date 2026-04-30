package dto;

import java.time.LocalDateTime;

public class ConversationSummary {
    private int conversationID;
    private int itemID;
    private String itemTitle;
    private int otherUserID;
    private String otherUserName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
    private String otherUserPicture;

    public ConversationSummary(int conversationID, int itemID, String itemTitle,
                               int otherUserID, String otherUserName,
                               String lastMessage, LocalDateTime lastMessageAt,
                               int unreadCount) {
        this.conversationID = conversationID;
        this.itemID = itemID;
        this.itemTitle = itemTitle;
        this.otherUserID = otherUserID;
        this.otherUserName = otherUserName;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
    }

    public int getConversationID() { return conversationID; }
    public int getItemID() { return itemID; }
    public String getItemTitle() { return itemTitle; }
    public int getOtherUserID() { return otherUserID; }
    public String getOtherUserName() { return otherUserName; }
    public String getLastMessage() { return lastMessage; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public int getUnreadCount() { return unreadCount; }

    public String getOtherUserPicture() { return otherUserPicture; }
    public void setOtherUserPicture(String otherUserPicture) { this.otherUserPicture = otherUserPicture; }

    public void setConversationID(int conversationID) { this.conversationID = conversationID; }
    public void setItemID(int itemID) { this.itemID = itemID; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public void setOtherUserID(int otherUserID) { this.otherUserID = otherUserID; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}