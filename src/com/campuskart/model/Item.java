package com.campuskart.model;

// A CampusKart listing.
// ★ RUBRIC: CO1 | Asg 2: demonstrates ALL THREE constructors — default, parameterized, copy
//            Asg 9 : final variable (itemId)
public class Item {

    private final int itemId;                     // ★ Asg 9: final — a stored item never changes its id
    private int sellerId;
    private String category, title, description, condition, status;
    private double price;

    public Item() {                               // ★ Asg 2: default constructor
        this.itemId = 0;
        this.status = "AVAILABLE";
    }

    // ★ Asg 2: parameterized constructor
    public Item(int itemId, int sellerId, String category, String title, String description,
                String condition, double price, String status) {
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.category = category;
        this.title = title;
        this.description = description;
        this.condition = condition;
        this.price = price;
        this.status = status;
    }

    // ★ Asg 2: copy constructor — builds an independent working copy of an existing item
    public Item(Item other) {
        this(other.itemId, other.sellerId, other.category, other.title,
             other.description, other.condition, other.price, other.status);
    }

    public final int getItemId() { return itemId; }   // ★ Asg 9: final method
    public int getSellerId() { return sellerId; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCondition() { return condition; }
    public String getStatus() { return status; }
    public double getPrice() { return price; }

    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
    public void setCategory(String category) { this.category = category; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCondition(String condition) { this.condition = condition; }
    public void setStatus(String status) { this.status = status; }
    public void setPrice(double price) { this.price = price; }
}
