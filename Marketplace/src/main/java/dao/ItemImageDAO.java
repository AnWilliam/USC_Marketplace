package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.ItemImage;
import util.DBUtil;

public class ItemImageDAO {
    public void create(int itemID, String imageUrl, int displayOrder) throws SQLException {
        String sql = "INSERT INTO ItemImages (itemID, image_url, display_order) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemID);
            stmt.setString(2, imageUrl);
            stmt.setInt(3, displayOrder);
            stmt.executeUpdate();
        }
    }

    public List<ItemImage> findByItemId(int itemID) throws SQLException {
        String sql = "SELECT * FROM ItemImages WHERE itemID = ? ORDER BY display_order ASC, imageID ASC";
        List<ItemImage> images = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    images.add(mapImage(rs));
                }
            }
        }
        return images;
    }

    public List<String> findUrlsByItemId(int itemID) throws SQLException {
        List<String> urls = new ArrayList<>();
        for (ItemImage image : findByItemId(itemID)) {
            urls.add(image.getImageUrl());
        }
        return urls;
    }

    private ItemImage mapImage(ResultSet rs) throws SQLException {
        Timestamp uploaded = rs.getTimestamp("uploaded_at");
        return new ItemImage(
            rs.getInt("imageID"),
            rs.getInt("itemID"),
            rs.getString("image_url"),
            rs.getInt("display_order"),
            uploaded == null ? null : uploaded.toLocalDateTime()
        );
    }
}
