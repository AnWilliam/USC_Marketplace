package model;

import java.time.LocalDateTime;

public class Message {

    private int messageID;
    private int conversationID;
    private int senderID;
    private String content;
    private LocalDateTime timestamp;

    public Message(int messageID, int conversationID, int senderID, String content, LocalDateTime timestamp) {
        this.messageID = messageID;
        this.conversationID = conversationID;
        this.senderID = senderID;
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getMessageID() { return messageID; }
    public void setMessageID(int messageID) { this.messageID = messageID; }

    public int getConversationID() { return conversationID; }
    public void setConversationID(int conversationID) { this.conversationID = conversationID; }

    public int getSenderID() { return senderID; }
    public void setSenderID(int senderID) { this.senderID = senderID; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public void sendMessage() {
        // TODO: Persist this message to the database and deliver to recipient
    }

    public void markAsRead() {
        // TODO: Update the message read status in the database
    }
}
