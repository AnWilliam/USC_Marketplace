package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.CategoryDAO;
import dao.ItemDAO;
import dao.ItemImageDAO;
import model.Item;
import util.ValidationUtil;

public class ItemService {
    private final ItemDAO itemDAO;
    private final CategoryDAO categoryDAO;
    private final ItemImageDAO itemImageDAO;

    public ItemService() {
        this(new ItemDAO(), new CategoryDAO(), new ItemImageDAO());
    }

    public ItemService(ItemDAO itemDAO, CategoryDAO categoryDAO, ItemImageDAO itemImageDAO) {
        this.itemDAO = itemDAO;
        this.categoryDAO = categoryDAO;
        this.itemImageDAO = itemImageDAO;
    }

    public Item createItem(int sellerID, int categoryID, String title, String description, BigDecimal price) throws SQLException {
        if (ValidationUtil.isBlank(title)) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (!ValidationUtil.isValidPrice(price)) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }
        if (categoryDAO.findById(categoryID) == null) {
            throw new IllegalArgumentException("Category does not exist.");
        }

        Item item = new Item();
        item.setSellerID(sellerID);
        item.setCategoryID(categoryID);
        item.setTitle(title.trim());
        item.setDescription(description);
        item.setPrice(price);
        item.setStatus("AVAILABLE");
        int itemID = itemDAO.create(item);
        return itemDAO.findById(itemID);
    }

    public List<Item> getAvailableItems() throws SQLException {
        return attachImages(itemDAO.findAvailableItems());
    }

    public Item getItemById(int itemID) throws SQLException {
        Item item = itemDAO.findById(itemID);
        if (item == null) {
            throw new IllegalArgumentException("Item not found.");
        }
        attachImages(item);
        return item;
    }

    public List<Item> searchItems(String keyword, Integer categoryID) throws SQLException {
        return attachImages(itemDAO.search(keyword, categoryID));
    }

    public void updateStatus(int itemID, int sellerID, String status) throws SQLException {
        if (!ValidationUtil.isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid item status.");
        }
        boolean updated = itemDAO.updateStatus(itemID, sellerID, status.toUpperCase());
        if (!updated) {
            throw new IllegalArgumentException("Item not found or you are not the seller.");
        }
    }

    public Item addItemImages(int itemID, int sellerID, List<String> imageUrls) throws SQLException {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return getItemById(itemID);
        }
        if (imageUrls.size() > 6) {
            throw new IllegalArgumentException("You can upload up to 6 images.");
        }

        Item item = itemDAO.findById(itemID);
        if (item == null || item.getSellerID() != sellerID) {
            throw new IllegalArgumentException("Item not found or you are not the seller.");
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            itemImageDAO.create(itemID, imageUrls.get(i), i + 1);
        }
        return getItemById(itemID);
    }

    private List<Item> attachImages(List<Item> items) throws SQLException {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            attachImages(item);
            result.add(item);
        }
        return result;
    }

    private void attachImages(Item item) throws SQLException {
        item.setImageUrls(itemImageDAO.findUrlsByItemId(item.getItemID()));
    }
}
