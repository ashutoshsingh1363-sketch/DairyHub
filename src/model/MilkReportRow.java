package model;

import java.time.LocalDate;

/** Read-only row used by the milk report table. */
public class MilkReportRow {
    private LocalDate collectionDate;
    private String shift;
    private int farmerId;
    private String farmerName;
    private String milkType;
    private double quantity;
    private double fat;
    private double snf;
    private double rate;
    private double totalAmount;

    public LocalDate getCollectionDate() { return collectionDate; }
    public void setCollectionDate(LocalDate collectionDate) { this.collectionDate = collectionDate; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
    public String getMilkType() { return milkType; }
    public void setMilkType(String milkType) { this.milkType = milkType; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }
    public double getSnf() { return snf; }
    public void setSnf(double snf) { this.snf = snf; }
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}
