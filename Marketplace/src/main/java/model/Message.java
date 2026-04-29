package model;

import java.time.LocalDateTime;

public class Message {
    private int messageID;
    private int conversationID;
    private int senderID;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;

    public Message() {
    }

    public Message(int messageID, int conversationID, int senderID, String content, LocalDateTime timestamp, boolean read) {
        this.messageID = messageID;
        this.conversationID = conversationID;
        this.senderID = senderID;
        this.content = content;
        this.timestamp = timestamp;
        this.read = read;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public int getConversationID() {
        return conversationID;
    }

    public void setConversationID(int conversationID) {
        this.conversationID = conversationID;
    }

    public int getSenderID() {
        return senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}