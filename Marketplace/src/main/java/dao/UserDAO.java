package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import model.User;
import util.DBUtil;

public class UserDAO {
    public int create(User user) throws SQLException {
        String sql = "INSERT INTO Users (email, password_hash, name, profile_picture, bio) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getProfilePicture());
            stmt.setString(5, user.getBio());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Creating user failed; no ID returned.");
    }

    public User findById(int userID) throws SQLException {
        String sql = "SELECT * FROM Users WHERE userID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public boolean updateProfile(int userID, String bio, String profilePicture) throws SQLException {
        String sql = "UPDATE Users SET bio = ?, profile_picture = ? WHERE userID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bio);
            stmt.setString(2, profilePicture);
            stmt.setInt(3, userID);
            return stmt.executeUpdate() > 0;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        return new User(
            rs.getInt("userID"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("name"),
            created == null ? null : created.toLocalDateTime(),
            rs.getString("profile_picture"),
            rs.getString("bio")
        );
    }
}