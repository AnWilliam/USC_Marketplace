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
import model.Wishlist;
import util.DBUtil;

public class WishlistDAO {

    private static final String WISHLIST_ITEM_SELECT = ""
        + "SELECT i.itemID, i.sellerID, i.categoryID, i.title, i.description, i.item_condition, i.photo_path, i.price, i.status, i.date_listed, "
        + "u.name AS sellerName, c.categoryName AS categoryName "
        + "FROM Items i "
        + "JOIN Users u ON i.sellerID = u.userID "
        + "JOIN Categories c ON i.categoryID = c.categoryID ";

    private static final String WISHLIST_ITEM_SELECT_LEGACY = ""
        + "SELECT i.itemID, i.sellerID, i.categoryID, i.title, i.description, i.price, i.status, i.date_listed, "
        + "u.name AS sellerName, c.categoryName AS categoryName "
        + "FROM Items i "
        + "JOIN Users u ON i.sellerID = u.userID "
        + "JOIN Categories c ON i.categoryID = c.categoryID ";

    private static boolean isMissingSchemaColumn(SQLException e) {
        String msg = e.getMessage();
        return msg != null && (msg.contains("Unknown column") || msg.contains("doesn't exist"));
    }

    /**
     * Adds an item to the wishlist for a user. If the item is already in the wishlist,
     * this method will throw a SQLException due to the unique constraint.
     *
     * @param userID the user ID
     * @param itemID the item ID
     * @return the wishlistID of the new entry
     * @throws SQLException if the item is already in the wishlist or database error occurs
     */
    public int addToWishlist(int userID, int itemID) throws SQLException {
        String sql = "INSERT INTO Wishlist (userID, itemID) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, itemID);
            if (isInWishlist(userID, itemID)) {
                throw new SQLException("Item already in wishlist");
            }
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Adding to wishlist failed; no ID returned.");
    }
    public int countWishlist(int userID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Wishlist WHERE userID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Removes an item from the user's wishlist.
     *
     * @param userID the user ID
     * @param itemID the item ID
     * @return true if the item was removed, false if it was not in the wishlist
     * @throws SQLException if a database error occurs
     */
    public boolean removeFromWishlist(int userID, int itemID) throws SQLException {
        String sql = "DELETE FROM Wishlist WHERE userID = ? AND itemID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, itemID);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if an item is in the user's wishlist.
     *
     * @param userID the user ID
     * @param itemID the item ID
     * @return true if the item is in the wishlist, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean isInWishlist(int userID, int itemID) throws SQLException {
        String sql = "SELECT 1 FROM Wishlist WHERE userID = ? AND itemID = ? LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, itemID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Gets all items in a user's wishlist, ordered by most recently added first.
     *
     * @param userID the user ID
     * @return a list of Item objects in the wishlist
     * @throws SQLException if a database error occurs
     */
    public List<Item> getWishlistItems(int userID) throws SQLException {
        try {
            return queryWishlistItems(userID, WISHLIST_ITEM_SELECT, true);
        } catch (SQLException e) {
            if (isMissingSchemaColumn(e)) {
                return queryWishlistItems(userID, WISHLIST_ITEM_SELECT_LEGACY, false);
            }
            throw e;
        }
    }

    private List<Item> queryWishlistItems(int userID, String selectBase, boolean extended) throws SQLException {
        String sql = selectBase
            + "INNER JOIN Wishlist w ON i.itemID = w.itemID "
            + "WHERE w.userID = ? "
            + "ORDER BY w.created_at DESC";
        List<Item> items = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs, extended));
                }
            }
        }
        return items;
    }

    /**
     * Gets the wishlist entry for a specific user and item.
     *
     * @param userID the user ID
     * @param itemID the item ID
     * @return the Wishlist object or null if not found
     * @throws SQLException if a database error occurs
     */
    public Wishlist findByUserAndItem(int userID, int itemID) throws SQLException {
        String sql = "SELECT wishlistID, userID, itemID, created_at FROM Wishlist WHERE userID = ? AND itemID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            stmt.setInt(2, itemID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapWishlist(rs) : null;
            }
        }
    }

    private Wishlist mapWishlist(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        return new Wishlist(
            rs.getInt("wishlistID"),
            rs.getInt("userID"),
            rs.getInt("itemID"),
            created == null ? null : created.toLocalDateTime()
        );
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
