package dao;

import database.DBConnection;
import model.User;
import utils.AppLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Handles login queries for admins and operators. */
public class UserDAO {
    private static final Logger LOGGER = AppLogger.getLogger(UserDAO.class);

    public Optional<User> authenticate(String username, String password) {
        Optional<User> admin = authenticateAdmin(username, password);
        if (admin.isPresent()) {
            return admin;
        }
        return authenticateOperator(username, password);
    }

    private Optional<User> authenticateAdmin(String username, String password) {
        String sql = "SELECT username FROM users WHERE username=? AND password=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                return Optional.empty();
            }
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(username, username, "Admin"));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Admin authentication failed", ex);
        }
        return Optional.empty();
    }

    private Optional<User> authenticateOperator(String username, String password) {
        String sql = "SELECT operator_id, operator_name FROM operators WHERE operator_id=? AND login_password=? AND status='Active'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) {
                return Optional.empty();
            }
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(rs.getString("operator_id"), rs.getString("operator_name"), "Operator"));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Operator authentication failed", ex);
        }
        return Optional.empty();
    }
}
