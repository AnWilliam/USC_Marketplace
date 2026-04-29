package service;

import java.sql.SQLException;
import java.util.List;

import dao.ConversationDAO;
import dao.ItemDAO;
import model.Conversation;
import model.Item;

public class ConversationService {
    private final ConversationDAO conversationDAO;
    private final ItemDAO itemDAO;

    public ConversationService() {
        this(new ConversationDAO(), new ItemDAO());
    }

    public ConversationService(ConversationDAO conversationDAO, ItemDAO itemDAO) {
        this.conversationDAO = conversationDAO;
        this.itemDAO = itemDAO;
    }

    public Conversation startConversation(int itemID, int buyerID) throws SQLException {
        Item item = itemDAO.findById(itemID);
        if (item == null) {
            throw new IllegalArgumentException("Item not found.");
        }
        if (item.getSellerID() == buyerID) {
            throw new IllegalArgumentException("You cannot contact yourself about your own item.");
        }

        Conversation existing = conversationDAO.findExisting(itemID, buyerID, item.getSellerID());
        if (existing != null) {
            return existing;
        }

        Conversation conversation = new Conversation();
        conversation.setItemID(itemID);
        conversation.setBuyerID(buyerID);
        conversation.setSellerID(item.getSellerID());
        int conversationID = conversationDAO.create(conversation);
        return conversationDAO.findById(conversationID);
    }

    public List<Conversation> getConversationsForUser(int userID) throws SQLException {
        return conversationDAO.findByUser(userID);
    }

    public Conversation getConversation(int conversationID, int userID) throws SQLException {
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