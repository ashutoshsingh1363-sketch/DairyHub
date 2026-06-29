package dao;

import database.DBConnection;
import model.MilkCollection;
import model.MilkReportRow;
import utils.AppLogger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Contains milk collection, rate lookup and report queries. */
public class MilkDAO {
    private static final Logger LOGGER = AppLogger.getLogger(MilkDAO.class);

    public static class DashboardSummary {
        private final double totalMilk;
        private final double totalAmount;

        public DashboardSummary(double totalMilk, double totalAmount) {
            this.totalMilk = totalMilk;
            this.totalAmount = totalAmount;
        }

        public double getTotalMilk() { return totalMilk; }
        public double getTotalAmount() { return totalAmount; }
    }

    public DashboardSummary getTodaySummary() {
        String sql = "SELECT IFNULL(SUM(quantity),0) totalMilk, IFNULL(SUM(total_amount),0) totalAmount FROM milk_collection WHERE collection_date = CURDATE()";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return new DashboardSummary(0, 0);
            }
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DashboardSummary(rs.getDouble("totalMilk"), rs.getDouble("totalAmount"));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Unable to load dashboard summary", ex);
        }
        return new DashboardSummary(0, 0);
    }

    public OptionalDouble findRate(double fat, double snf) {
        String sql = "SELECT rate FROM rate_chart WHERE fat=? AND snf=?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return OptionalDouble.empty();
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, fat);
                ps.setDouble(2, snf);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return OptionalDouble.of(rs.getDouble("rate"));
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Unable to find rate", ex);
        }
        return OptionalDouble.empty();
    }

    public boolean collectionExists(int farmerId, LocalDate collectionDate, String milkType, String shift) throws Exception {
        String sql = "SELECT COUNT(*) FROM milk_collection WHERE farmer_id=? AND collection_date=? AND milk_type=? AND shift=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                throw new IllegalStateException("Database connection failed.");
            }
            ps.setInt(1, farmerId);
            ps.setDate(2, Date.valueOf(collectionDate));
            ps.setString(3, milkType);
            ps.setString(4, shift);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void saveCollection(MilkCollection collection) throws Exception {
        String sql = "INSERT INTO milk_collection (farmer_id,shift,milk_type,quantity,fat,snf,rate,total_amount,collection_date) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                throw new IllegalStateException("Database connection failed.");
            }
            ps.setInt(1, collection.getFarmerId());
            ps.setString(2, collection.getShift());
            ps.setString(3, collection.getMilkType());
            ps.setDouble(4, collection.getQuantity());
            ps.setDouble(5, collection.getFat());
            ps.setDouble(6, collection.getSnf());
            ps.setDouble(7, collection.getRate());
            ps.setDouble(8, collection.getTotalAmount());
            ps.setDate(9, Date.valueOf(collection.getCollectionDate()));
            ps.executeUpdate();
        }
    }

    public List<MilkReportRow> findReportRows(LocalDate fromDate, LocalDate toDate, String selectedShift, String selectedMilkType) throws Exception {
        List<MilkReportRow> rows = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT m.collection_date, m.shift, m.farmer_id, f.farmer_name, m.milk_type, m.quantity, m.fat, m.snf, m.rate, m.total_amount FROM milk_collection m LEFT JOIN farmers f ON m.farmer_id = f.farmer_id WHERE m.collection_date BETWEEN ? AND ?");
        if (!"All Shifts".equals(selectedShift)) {
            sql.append(" AND m.shift = ?");
        }
        if (!"All Types".equals(selectedMilkType)) {
            sql.append(" AND m.milk_type = ?");
        }
        sql.append(" ORDER BY m.collection_date");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (con == null) {
                throw new IllegalStateException("Database connection failed.");
            }
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));
            int index = 3;
            if (!"All Shifts".equals(selectedShift)) {
                ps.setString(index++, selectedShift);
            }
            if (!"All Types".equals(selectedMilkType)) {
                ps.setString(index, selectedMilkType);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MilkReportRow row = new MilkReportRow();
                    row.setCollectionDate(rs.getDate("collection_date").toLocalDate());
                    row.setShift(rs.getString("shift"));
                    row.setFarmerId(rs.getInt("farmer_id"));
                    row.setFarmerName(rs.getString("farmer_name"));
                    row.setMilkType(rs.getString("milk_type"));
                    row.setQuantity(rs.getDouble("quantity"));
                    row.setFat(rs.getDouble("fat"));
                    row.setSnf(rs.getDouble("snf"));
                    row.setRate(rs.getDouble("rate"));
                    row.setTotalAmount(rs.getDouble("total_amount"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
