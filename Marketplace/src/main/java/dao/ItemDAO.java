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

    /** Requires item_condition + photo_path columns (newer schema). */
    private static final String ITEM_SELECT_EXTENDED = ""
        + "SELECT i.itemID, i.sellerID, i.categoryID, i.title, i.description, i.item_condition, i.photo_path, i.price, i.status, i.date_listed, "
        + "u.name AS sellerName, c.categoryName AS categoryName "
        + "FROM Items i "
        + "JOIN Users u ON i.sellerID = u.userID "
        + "JOIN Categories c ON i.categoryID = c.categoryID ";

    /** Original Items table shape (no item_condition / photo_path). */
    private static final String ITEM_SELECT_LEGACY = ""
        + "SELECT i.itemID, i.sellerID, i.categoryID, i.title, i.description, i.price, i.status, i.date_listed, "
        + "u.name AS sellerName, c.categoryName AS categoryName "
        + "FROM Items i "
        + "JOIN Users u ON i.sellerID = u.userID "
        + "JOIN Categories c ON i.categoryID = c.categoryID ";

    private static boolean isMissingSchemaColumn(SQLException e) {
        String msg = e.getMessage();
        return msg != null && (msg.contains("Unknown column") || msg.contains("doesn't exist"));
    }

    public int create(Item item) throws SQLException {
        try {
            return insertExtended(item);
        } catch (SQLException e) {
            if (isMissingSchemaColumn(e)) {
                return insertLegacy(item);
            }
            throw e;
        }
    }

    private int insertExtended(Item item) throws SQLException {
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

    private int insertLegacy(Item item) throws SQLException {
        String sql = "INSERT INTO Items (sellerID, categoryID, title, description, price, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.getSellerID());
            stmt.setInt(2, item.getCategoryID());
            stmt.setString(3, item.getTitle());
            stmt.setString(4, item.getDescription());
            stmt.setBigDecimal(5, item.getPrice());
            stmt.setString(6, item.getStatus());
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
        try {
            return queryById(itemID, ITEM_SELECT_EXTENDED, true);
        } catch (SQLException e) {
            if (isMissingSchemaColumn(e)) {
                return queryById(itemID, ITEM_SELECT_LEGACY, false);
            }
            throw e;
        }
    }

    private Item queryById(int itemID, String selectBase, boolean extended) throws SQLException {
        String sql = selectBase + "WHERE i.itemID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapItem(rs, extended) : null;
            }
        }
    }

    public List<Item> findAvailableItems() throws SQLException {
        try {
            return queryList(ITEM_SELECT_EXTENDED + "WHERE i.status = 'AVAILABLE' ORDER BY i.date_listed DESC", true);
        } catch (SQLException e) {
            if (isMissingSchemaColumn(e)) {
                return queryList(ITEM_SELECT_LEGACY + "WHERE i.status = 'AVAILABLE' ORDER BY i.date_listed DESC", false);
            }
            throw e;
        }
    }

    public List<Item> search(String keyword, Integer categoryID) throws SQLException {
        StringBuilder ext = new StringBuilder(ITEM_SELECT_EXTENDED).append("WHERE i.status = 'AVAILABLE'");
        StringBuilder leg = new StringBuilder(ITEM_SELECT_LEGACY).append("WHERE i.status = 'AVAILABLE'");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            ext.append(" AND (i.title LIKE ? OR i.description LIKE ?)");
            leg.append(" AND (i.title LIKE ? OR i.description LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (categoryID != null) {
            ext.append(" AND i.categoryID = ?");
            leg.append(" AND i.categoryID = ?");
            params.add(categoryID);
        }
        ext.append(" ORDER BY i.date_listed DESC");
        leg.append(" ORDER BY i.date_listed DESC");

        try {
            return queryListWithParams(ext.toString(), true, params);
        } catch (SQLException e) {
            if (isMissingSchemaColumn(e)) {
                return queryListWithParams(leg.toString(), false, params);
            }
            throw e;
        }
    }

    private List<Item> queryList(String sql, boolean extended) throws SQLException {
        List<Item> items = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                items.add(mapItem(rs, extended));
            }
        }
        return items;
    }

    private List<Item> queryListWithParams(String sql, boolean extended, List<Object> params) throws SQLException {
        List<Item> items = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs, extended));
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

    public boolean updatePhotoPath(int itemID, int sellerID, String photoPath) throws SQLException {
        String sql = "UPDATE Items SET photo_path = ? WHERE itemID = ? AND sellerID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, photoPath);
            stmt.setInt(2, itemID);
            stmt.setInt(3, sellerID);
            return stmt.executeUpdate() > 0;
        }
    }

    private Item mapItem(ResultSet rs, boolean extended) throws SQLException {
        Timestamp listed = rs.getTimestamp("date_listed");
        String condition = null;
        if (extended) {
            condition = rs.getString("item_condition");
        }
        Item item = new Item(
            rs.getInt("itemID"),
            rs.getInt("sellerID"),
            rs.getInt("categoryID"),
            rs.getString("title"),
            rs.getString("description"),
            condition,
            rs.getBigDecimal("price"),
            rs.getString("status"),
            listed == null ? null : listed.toLocalDateTime()
        );
        item.setSellerName(rs.getString("sellerName"));
        item.setCategoryName(rs.getString("categoryName"));
        if (extended) {
            item.setPhotoPath(rs.getString("photo_path"));
        }
        return item;
    }
}
