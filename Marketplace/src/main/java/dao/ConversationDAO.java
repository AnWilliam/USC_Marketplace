package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Conversation;
import util.DBUtil;

public class ConversationDAO {
    public int create(Conversation conversation) throws SQLException {
        String sql = "INSERT INTO Conversations (itemID, buyerID, sellerID) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, conversation.getItemID());
            stmt.setInt(2, conversation.getBuyerID());
            stmt.setInt(3, conversation.getSellerID());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Creating conversation failed; no ID returned.");
    }

    public Conversation findById(int conversationID) throws SQLException {
        String sql = "SELECT * FROM Conversations WHERE conversationID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapConversation(rs) : null;
            }
        }
    }

    public Conversation findExisting(int itemID, int buyerID, int sellerID) throws SQLException {
        String sql = "SELECT * FROM Conversations WHERE itemID = ? AND buyerID = ? AND sellerID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemID);
            stmt.setInt(2, buyerID);
            stmt.setInt(3, sellerID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapConversation(rs) : null;
            }
        }
    }

    public List<Conversation> findByUser(int userID) throws SQLException {
        String sql = "SELECT * FROM Conversations WHERE buyerID = ? OR sellerID = ? ORDER BY COALESCE(last_message_at, created_at) DESC";
        List<Conversation> conversations = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    conversations.add(mapConversation(rs));
                }
            }
        }
        return conversations;
    }

    public List<dto.ConversationSummary> findSummariesByUser(int userID) throws SQLException {
    String sql =
        "SELECT " +
        "c.conversationID, " +
        "c.itemID, " +
        "i.title AS itemTitle, " +
        "CASE WHEN c.buyerID = ? THEN c.sellerID ELSE c.buyerID END AS otherUserID, " +
        "u.name AS otherUserName, " +
        "(SELECT m.content FROM Messages m " +
        " WHERE m.conversationID = c.conversationID " +
        " ORDER BY m.timestamp DESC LIMIT 1) AS lastMessage, " +
        "COALESCE(c.last_message_at, c.created_at) AS lastMessageAt, " +
        "(SELECT COUNT(*) FROM Messages unread " +
        " WHERE unread.conversationID = c.conversationID " +
        " AND unread.senderID <> ? " +
        " AND unread.is_read = FALSE) AS unreadCount " +
        "FROM Conversations c " +
        "JOIN Items i ON c.itemID = i.itemID " +
        "JOIN Users u ON u.userID = CASE WHEN c.buyerID = ? THEN c.sellerID ELSE c.buyerID END " +
        "WHERE c.buyerID = ? OR c.sellerID = ? " +
        "ORDER BY COALESCE(c.last_message_at, c.created_at) DESC";

    List<dto.ConversationSummary> summaries = new ArrayList<>();

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, userID);
        stmt.setInt(2, userID);
        stmt.setInt(3, userID);
        stmt.setInt(4, userID);
        stmt.setInt(5, userID); 

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Timestamp lastMessageAt = rs.getTimestamp("lastMessageAt");

                summaries.add(new dto.ConversationSummary(
                    rs.getInt("conversationID"),
                    rs.getInt("itemID"),
                    rs.getString("itemTitle"),
                    rs.getInt("otherUserID"),
                    rs.getString("otherUserName"),
                    rs.getString("lastMessage"),
                    lastMessageAt == null ? null : lastMessageAt.toLocalDateTime(),
                    rs.getInt("unreadCount")
                ));
            }
        }
    }

    return summaries;
}

    public boolean updateLastMessageAt(int conversationID) throws SQLException {
        String sql = "UPDATE Conversations SET last_message_at = CURRENT_TIMESTAMP WHERE conversationID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationID);
            return stmt.executeUpdate() > 0;
        }
    }

    private Conversation mapConversation(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp lastMessage = rs.getTimestamp("last_message_at");
        return new Conversation(
            rs.getInt("conversationID"),
            rs.getInt("itemID"),
            rs.getInt("buyerID"),
            rs.getInt("sellerID"),
            created == null ? null : created.toLocalDateTime(),
            lastMessage == null ? null : lastMessage.toLocalDateTime()
        );
    }
}