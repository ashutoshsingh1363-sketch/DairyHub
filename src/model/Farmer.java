package model;

/** Core farmer profile used across collection, payment and reports. */
public class Farmer {
    private int farmerId;
    private String farmerName;
    private String mobile;
    private String village;
    private String milkType;
    private String status;
    private String photoPath;

    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getMilkType() { return milkType; }
    public void setMilkType(String milkType) { this.milkType = milkType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}
