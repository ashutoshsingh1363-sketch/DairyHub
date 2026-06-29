package dao;

import database.DBConnection;
import model.Payment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

/** Handles farmer payment calculations and persistence. */
public class PaymentDAO {
    public Payment calculatePayment(int farmerId, LocalDate fromDate, LocalDate toDate) throws Exception {
        String sql = "SELECT IFNULL(SUM(quantity),0) AS total_milk, IFNULL(SUM(total_amount),0) AS total_amount FROM milk_collection WHERE farmer_id=? AND collection_date BETWEEN ? AND ?";
        Payment payment = new Payment();
        payment.setFarmerId(farmerId);
        payment.setFromDate(fromDate);
        payment.setToDate(toDate);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                throw new IllegalStateException("Database connection failed.");
            }
            ps.setInt(1, farmerId);
            ps.setDate(2, Date.valueOf(fromDate));
            ps.setDate(3, Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    payment.setTotalMilk(rs.getDouble("total_milk"));
                    payment.setTotalAmount(rs.getDouble("total_amount"));
                }
            }
        }
        return payment;
    }

    public boolean paymentExists(int farmerId, LocalDate fromDate, LocalDate toDate) throws Exception {
        String sql = "SELECT COUNT(*) FROM payments WHERE farmer_id=? AND from_date=? AND to_date=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                throw new IllegalStateException("Database connection failed.");
            }
            ps.setInt(1, farmerId);
            ps.setDate(2, Date.valueOf(fromDate));
            ps.setDate(3, Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void savePayment(Payment payment) throws Exception {
        String sql = "INSERT INTO payments(farmer_id,from_date,to_date,total_milk,total_amount,payment_date,remarks) VALUES(?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                throw new IllegalStateException("Database connection failed.");
            }
            ps.setInt(1, payment.getFarmerId());
            ps.setDate(2, Date.valueOf(payment.getFromDate()));
            ps.setDate(3, Date.valueOf(payment.getToDate()));
            ps.setDouble(4, payment.getTotalMilk());
            ps.setDouble(5, payment.getTotalAmount());
            ps.setDate(6, Date.valueOf(payment.getPaymentDate()));
            ps.setString(7, payment.getRemarks());
            ps.executeUpdate();
        }
    }
}
