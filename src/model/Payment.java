package model;

public class Payment {
    private int    paymentId;
    private int    milestoneId;
    private double amount;
    private String status;

    // Constructor
    public Payment(int paymentId, int milestoneId,
                   double amount, String status) {
        this.paymentId   = paymentId;
        this.milestoneId = milestoneId;
        this.amount      = amount;
        this.status      = status;
    }

    // Getters
    public int    getPaymentId()   { return paymentId; }
    public int    getMilestoneId() { return milestoneId; }
    public double getAmount()      { return amount; }
    public String getStatus()      { return status; }

    @Override
    public String toString() {
        return "Payment #" + paymentId + " ₹" + amount + " [" + status + "]";
    }
}