package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Item {
    private int itemID;
    private int sellerID;
    private int categoryID;
    private String title;
    private String description;
    private String itemCondition;
    private BigDecimal price;
    private String status;
    private LocalDateTime dateListed;
    private String sellerName;
    private String categoryName;

    public Item() {
    }

    public Item(int itemID, int sellerID, int categoryID, String title, String description, String itemCondition, BigDecimal price, String status, LocalDateTime dateListed) {
        this.itemID = itemID;
        this.sellerID = sellerID;
        this.categoryID = categoryID;
        this.title = title;
        this.description = description;
        this.itemCondition = itemCondition;
        this.price = price;
        this.status = status;
        this.dateListed = dateListed;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getSellerID() {
        return sellerID;
    }

    public void setSellerID(int sellerID) {
        this.sellerID = sellerID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDateListed() {
        return dateListed;
    }

    public void setDateListed(LocalDateTime dateListed) {
        this.dateListed = dateListed;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
