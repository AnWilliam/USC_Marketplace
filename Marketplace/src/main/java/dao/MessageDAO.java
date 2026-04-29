package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.Message;
import util.DBUtil;

public class MessageDAO {
    public int create(Message message) throws SQLException {
        String sql = "INSERT INTO Messages (conversationID, senderID, content) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, message.getConversationID());
            stmt.setInt(2, message.getSenderID());
            stmt.setString(3, message.getContent());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Creating message failed; no ID returned.");
    }

    public Message findById(int messageID) throws SQLException {
        String sql = "SELECT * FROM Messages WHERE messageID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, messageID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapMessage(rs) : null;
            }
        }
    }

    public List<Message> findByConversationId(int conversationID) throws SQLException {
        String sql = "SELECT m.*, u.name AS senderName FROM Messages m JOIN Users u ON m.senderID = u.userID WHERE m.conversationID = ? ORDER BY m.timestamp ASC";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }
        }
        return messages;
    }

    public boolean markAsRead(int conversationID, int readerID) throws SQLException {
        String sql = "UPDATE Messages SET is_read = TRUE WHERE conversationID = ? AND senderID <> ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationID);
            stmt.setInt(2, readerID);
            return stmt.executeUpdate() >= 0;
        }
    }

    private Message mapMessage(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("timestamp");
        Message message = new Message(
            rs.getInt("messageID"),
            rs.getInt("conversationID"),
            rs.getInt("senderID"),
            rs.getString("content"),
            timestamp == null ? null : timestamp.toLocalDateTime(),
            rs.getBoolean("is_read")
        );
        // set senderName if present in ResultSet
        try {
            String senderName = rs.getString("senderName");
            message.setSenderName(senderName);
        } catch (SQLException e) {
            // Column not present; ignore to keep backward compatibility
        }
        return message;
    }
}