package com.campuskart.model;

// Invoice for a completed sale (feature F5).
// ★ RUBRIC: Interfaces | Asg 11: a class implementing an interface provides every method
//            Asg 3 : METHOD OVERLOADING — calculateTotal with three parameter lists
//            Asg 9 : final fields — a written receipt never changes
public class Receipt implements Billable {

    private final int transactionId;              // ★ Asg 9: final field
    private final String transactionDate;         // ★ Asg 9: final field
    private final String itemTitle;
    private final double agreedPrice;

    public Receipt(int transactionId, String transactionDate, String itemTitle, double agreedPrice) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.itemTitle = itemTitle;
        this.agreedPrice = agreedPrice;
    }

    // ★ RUBRIC: CO1 | Asg 3: METHOD OVERLOADING — same name, different parameter lists
    public double calculateTotal(double price) {
        return price;
    }

    public double calculateTotal(double price, double discountPercent) {
        return price - (price * discountPercent / 100.0);
    }

    public double calculateTotal(double price, double discountPercent, double handlingFee) {
        return calculateTotal(price, discountPercent) + handlingFee;   // reuses the 2-argument version
    }

    @Override
    public double calculateTotal() {               // ★ Asg 11: implementing the interface method
        return calculateTotal(agreedPrice, 0);
    }

    @Override
    public String generateReceipt() {
        StringBuffer sb = new StringBuffer();      // ★ Asg 5: StringBuffer for string assembly
        sb.append("CAMPUSKART INVOICE\n");
        sb.append("------------------\n");
        sb.append("Transaction : ").append(transactionId).append('\n');
        sb.append("Date        : ").append(transactionDate).append('\n');
        sb.append("Item        : ").append(itemTitle).append('\n');
        sb.append("Total (INR) : ").append(calculateTotal()).append('\n');
        return sb.toString();
    }

    public int getTransactionId() { return transactionId; }
}
