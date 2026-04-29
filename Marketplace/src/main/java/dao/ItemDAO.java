package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.Item;
import util.DBUtil;

public class ItemDAO {
    private static final String ITEM_SELECT_BASE = ""
        + "SELECT i.itemID, i.sellerID, i.categoryID, i.title, i.description, i.item_condition, i.price, i.status, i.date_listed, "
        + "u.name AS sellerName, c.categoryName AS categoryName "
        + "FROM Items i "
        + "JOIN Users u ON i.sellerID = u.userID "
        + "JOIN Categories c ON i.categoryID = c.categoryID ";

    public int create(Item item) throws SQLException {
        String sql = "INSERT INTO Items (sellerID, categoryID, title, description, item_condition, price, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.getSellerID());
            stmt.setInt(2, item.getCategoryID());
            stmt.setString(3, item.getTitle());
            stmt.setString(4, item.getDescription());
            stmt.setString(5, item.getItemCondition());
            stmt.setBigDecimal(6, item.getPrice());
            stmt.setString(7, item.getStatus());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Creating item failed; no ID returned.");
    }

    public Item findById(int itemID) throws SQLException {
        String sql = ITEM_SELECT_BASE + "WHERE i.itemID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapItem(rs) : null;
            }
        }
    }

    public List<Item> findAvailableItems() throws SQLException {
        String sql = ITEM_SELECT_BASE + "WHERE i.status = 'AVAILABLE' ORDER BY i.date_listed DESC";
        List<Item> items = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                items.add(mapItem(rs));
            }
        }
        return items;
    }

    public List<Item> search(String keyword, Integer categoryID) throws SQLException {
        StringBuilder sql = new StringBuilder(ITEM_SELECT_BASE).append("WHERE i.status = 'AVAILABLE'");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (i.title LIKE ? OR i.description LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (categoryID != null) {
            sql.append(" AND i.categoryID = ?");
            params.add(categoryID);
        }
        sql.append(" ORDER BY i.date_listed DESC");

        List<Item> items = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        }
        return items;
    }

    public boolean updateStatus(int itemID, int sellerID, String status) throws SQLException {
        String sql = "UPDATE Items SET status = ? WHERE itemID = ? AND sellerID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, itemID);
            stmt.setInt(3, sellerID);
            return stmt.executeUpdate() > 0;
        }
    }

    private Item mapItem(ResultSet rs) throws SQLException {
        Timestamp listed = rs.getTimestamp("date_listed");
        Item item = new Item(
            rs.getInt("itemID"),
            rs.getInt("sellerID"),
            rs.getInt("categoryID"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("item_condition"),
            rs.getBigDecimal("price"),
            rs.getString("status"),
            listed == null ? null : listed.toLocalDateTime()
        );
        item.setSellerName(rs.getString("sellerName"));
        item.setCategoryName(rs.getString("categoryName"));
        return item;
    }
}
