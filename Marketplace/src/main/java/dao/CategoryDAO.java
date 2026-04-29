package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Category;
import util.DBUtil;

public class CategoryDAO {
    public int create(Category category) throws SQLException {
        String sql = "INSERT INTO Categories (categoryName, description) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Creating category failed; no ID returned.");
    }

    public Category findById(int categoryID) throws SQLException {
        String sql = "SELECT * FROM Categories WHERE categoryID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapCategory(rs) : null;
            }
        }
    }

    public Category findByName(String categoryName) throws SQLException {
        String sql = "SELECT * FROM Categories WHERE categoryName = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapCategory(rs) : null;
            }
        }
    }

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT * FROM Categories ORDER BY categoryName";
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(mapCategory(rs));
            }
        }
        return categories;
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        return new Category(
            rs.getInt("categoryID"),
            rs.getString("categoryName"),
            rs.getString("description")
        );
    }
}