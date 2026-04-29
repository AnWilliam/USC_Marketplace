package service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import dao.CategoryDAO;
import dao.ItemDAO;
import model.Item;
import util.ValidationUtil;

public class ItemService {
    private final ItemDAO itemDAO;
    private final CategoryDAO categoryDAO;

    public ItemService() {
        this(new ItemDAO(), new CategoryDAO());
    }

    public ItemService(ItemDAO itemDAO, CategoryDAO categoryDAO) {
        this.itemDAO = itemDAO;
        this.categoryDAO = categoryDAO;
    }

    public Item createItem(int sellerID, int categoryID, String title, String description, BigDecimal price, String itemCondition) throws SQLException {
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
        item.setItemCondition(itemCondition != null && !itemCondition.trim().isEmpty() ? itemCondition.trim() : null);
        item.setPrice(price);
        item.setStatus("AVAILABLE");
        int itemID = itemDAO.create(item);
        return itemDAO.findById(itemID);
    }

    public List<Item> getAvailableItems() throws SQLException {
        return itemDAO.findAvailableItems();
    }

    public Item getItemById(int itemID) throws SQLException {
        Item item = itemDAO.findById(itemID);
        if (item == null) {
            throw new IllegalArgumentException("Item not found.");
        }
        return item;
    }

    public List<Item> searchItems(String keyword, Integer categoryID) throws SQLException {
        return itemDAO.search(keyword, categoryID);
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
}
