package model;

import java.time.LocalDate;

/** Represents farmer payment details for a date range. */
public class Payment {
    private int farmerId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private double totalMilk;
    private double totalAmount;
    private LocalDate paymentDate;
    private String remarks;

    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public double getTotalMilk() { return totalMilk; }
    public void setTotalMilk(double totalMilk) { this.totalMilk = totalMilk; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
