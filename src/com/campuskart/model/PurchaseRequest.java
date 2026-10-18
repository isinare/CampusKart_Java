package com.campuskart.model;

// A buyer's offer on an item (feature F5).
// ★ Asg 9: final variable (requestId)
public class PurchaseRequest {

    private final int requestId;
    private int itemId, buyerId, sellerId;
    private String title, status;
    private double offeredPrice;

    public PurchaseRequest() {
        this.requestId = 0;
    }

    public PurchaseRequest(int requestId, int itemId, int buyerId, int sellerId,
                           String title, double offeredPrice, String status) {
        this.requestId = requestId;
        this.itemId = itemId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.title = title;
        this.offeredPrice = offeredPrice;
        this.status = status;
    }

    public final int getRequestId() { return requestId; }
    public int getItemId() { return itemId; }
    public int getBuyerId() { return buyerId; }
    public int getSellerId() { return sellerId; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public double getOfferedPrice() { return offeredPrice; }
}
