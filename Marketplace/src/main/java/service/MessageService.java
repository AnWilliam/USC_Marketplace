package service;

import java.sql.SQLException;
import java.util.List;

import dao.ConversationDAO;
import dao.MessageDAO;
import model.Conversation;
import model.Message;
import util.ValidationUtil;

public class MessageService {
    private final MessageDAO messageDAO;
    private final ConversationDAO conversationDAO;

    public MessageService() {
        this(new MessageDAO(), new ConversationDAO());
    }

    public MessageService(MessageDAO messageDAO, ConversationDAO conversationDAO) {
        this.messageDAO = messageDAO;
        this.conversationDAO = conversationDAO;
    }

    public Message sendMessage(int conversationID, int senderID, String content) throws SQLException {
        if (ValidationUtil.isBlank(content)) {
            throw new IllegalArgumentException("Message content is required.");
        }
        Conversation conversation = requireConversationAccess(conversationID, senderID);

        Message message = new Message();
        message.setConversationID(conversation.getConversationID());
        message.setSenderID(senderID);
        message.setContent(content.trim());
        int messageID = messageDAO.create(message);
        conversationDAO.updateLastMessageAt(conversationID);
        return messageDAO.findById(messageID);
    }

    public List<Message> getMessages(int conversationID, int userID) throws SQLException {
        requireConversationAccess(conversationID, userID);
        return messageDAO.findByConversationId(conversationID);
    }

    public void markAsRead(int conversationID, int userID) throws SQLException {
        requireConversationAccess(conversationID, userID);
        messageDAO.markAsRead(conversationID, userID);
    }

    private Conversation requireConversationAccess(int conversationID, int userID) throws SQLException {
        Conversation conversation = conversationDAO.findById(conversationID);
        if (conversation == null) {
            throw new IllegalArgumentException("Conversation not found.");
        }
        if (conversation.getBuyerID() != userID && conversation.getSellerID() != userID) {
            throw new IllegalArgumentException("You do not have access to this conversation.");
        }
        return conversation;
    }
}