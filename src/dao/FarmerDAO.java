package dao;

import database.DBConnection;
import model.Farmer;
import utils.AppLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Provides farmer lookup and persistence operations. */
public class FarmerDAO {
    private static final Logger LOGGER = AppLogger.getLogger(FarmerDAO.class);

    public Optional<String> findFarmerNameById(int farmerId) {
        String sql = "SELECT farmer_name FROM farmers WHERE farmer_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                return Optional.empty();
            }
            ps.setInt(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("farmer_name"));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Unable to find farmer", ex);
        }
        return Optional.empty();
    }

    public Optional<Farmer> findById(int farmerId) {
        String sql = "SELECT farmer_id, farmer_name, mobile, village, milk_type, status, photo_path FROM farmers WHERE farmer_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                return Optional.empty();
            }
            ps.setInt(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Farmer farmer = new Farmer();
                    farmer.setFarmerId(rs.getInt("farmer_id"));
                    farmer.setFarmerName(rs.getString("farmer_name"));
                    farmer.setMobile(rs.getString("mobile"));
                    farmer.setVillage(rs.getString("village"));
                    farmer.setMilkType(rs.getString("milk_type"));
                    farmer.setStatus(rs.getString("status"));
                    farmer.setPhotoPath(rs.getString("photo_path"));
                    return Optional.of(farmer);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Unable to load farmer", ex);
        }
        return Optional.empty();
    }
}
