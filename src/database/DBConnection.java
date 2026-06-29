package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import utils.AppPaths;
import utils.AppLogger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {

    private static final Logger LOGGER = AppLogger.getLogger(DBConnection.class);

    private static final String URL = "jdbc:mysql://localhost:3306/dairyhub";
    private static final String USER = "root";
    private static final String PASSWORD = "Ashu@1363";
    private static final String[] ALTERNATIVE_PASSWORDS = {"", "root", "admin", "123456", "1234"};

    private static String activePassword = PASSWORD;
    private static boolean isInitialized = false;

    public static Connection getConnection() {
        try {
            Connection con = tryConnectionWithPassword(activePassword);
            if (con != null) {
                if (!isInitialized) {
                    initializeTables(con);
                    isInitialized = true;
                }
                return con;
            }
        } catch (Exception e) {
            // Fall through to check other passwords
        }

        // Try alternative passwords
        for (String pass : ALTERNATIVE_PASSWORDS) {
            if (pass.equals(PASSWORD)) continue;
            try {
                Connection con = tryConnectionWithPassword(pass);
                if (con != null) {
                    activePassword = pass; // lock in the working password
                    if (!isInitialized) {
                        initializeTables(con);
                        isInitialized = true;
                    }
                    return con;
                }
            } catch (Exception e) {
                // Try next
            }
        }
        return null;
    }

    private static Connection tryConnectionWithPassword(String password) throws Exception {
        try {
            return DriverManager.getConnection(URL, USER, password);
        } catch (Exception e) {
            // Try connecting to raw server and creating the database
            Connection rawCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, password);
            if (rawCon != null) {
                try (Statement stmt = rawCon.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS dairyhub");
                }
                rawCon.close();
                return DriverManager.getConnection(URL, USER, password);
            }
            throw e;
        }
    }

    private static void initializeTables(Connection con) {
        try (Statement stmt = con.createStatement()) {
            // 1. Create users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) PRIMARY KEY, " +
                    "password VARCHAR(50) NOT NULL)");

            // Insert default user if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('admin', 'admin')");
                    stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('Ashu1363', 'Ashu@1363')");
                }
            }

            // 2. Create farmers table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS farmers (" +
                    "farmer_id INT PRIMARY KEY, " +
                    "farmer_name VARCHAR(100) NOT NULL, " +
                    "father_name VARCHAR(100), " +
                    "gender VARCHAR(10), " +
                    "dob VARCHAR(20), " +
                    "mobile VARCHAR(15), " +
                    "alternate_mobile VARCHAR(15), " +
                    "village VARCHAR(100), " +
                    "post VARCHAR(100), " +
                    "block VARCHAR(100), " +
                    "district VARCHAR(100), " +
                    "state VARCHAR(100), " +
                    "pin_code VARCHAR(10), " +
                    "bank_name VARCHAR(100), " +
                    "branch VARCHAR(100), " +
                    "ifsc VARCHAR(20), " +
                    "account_no VARCHAR(30), " +
                    "bank_account VARCHAR(30), " +
                    "account_holder VARCHAR(100), " +
                    "aadhaar VARCHAR(20), " +
                    "pan VARCHAR(20), " +
                    "milk_type VARCHAR(20), " +
                    "center_name VARCHAR(100), " +
                    "joining_date VARCHAR(20), " +
                    "status VARCHAR(20), " +
                    "photo_path VARCHAR(255))");

            // 3. Create rate_chart table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS rate_chart (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "fat DOUBLE NOT NULL, " +
                    "snf DOUBLE NOT NULL, " +
                    "rate DOUBLE NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE KEY uq_rate_chart (fat, snf))");

            // 4. Create milk_collection table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS milk_collection (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "farmer_id INT NOT NULL, " +
                    "shift VARCHAR(15) NOT NULL, " +
                    "milk_type VARCHAR(15) NOT NULL, " +
                    "quantity DOUBLE NOT NULL, " +
                    "fat DOUBLE NOT NULL, " +
                    "snf DOUBLE NOT NULL, " +
                    "rate DOUBLE NOT NULL, " +
                    "total_amount DOUBLE NOT NULL, " +
                    "collection_date DATE NOT NULL)");

            // 5. Create payments table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS payments (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "farmer_id INT NOT NULL, " +
                    "from_date DATE NOT NULL, " +
                    "to_date DATE NOT NULL, " +
                    "total_milk DOUBLE NOT NULL, " +
                    "total_amount DOUBLE NOT NULL, " +
                    "payment_date DATE NOT NULL, " +
                    "remarks TEXT)");

            // 6. Create bills table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bills (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "farmer_name VARCHAR(100) NOT NULL, " +
                    "amount DOUBLE NOT NULL, " +
                    "due_date DATE NOT NULL, " +
                    "remarks TEXT, " +
                    "status VARCHAR(20) DEFAULT 'Pending', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 7. Create feeds table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS feeds (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "feed_name VARCHAR(100) NOT NULL, " +
                    "quantity DOUBLE NOT NULL, " +
                    "price DOUBLE NOT NULL, " +
                    "supplier VARCHAR(100), " +
                    "purchase_date DATE NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 8. Create operators table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS operators (" +
                    "operator_id VARCHAR(30) PRIMARY KEY, " +
                    "operator_name VARCHAR(100) NOT NULL, " +
                    "mobile VARCHAR(15) NOT NULL, " +
                    "role_name VARCHAR(50), " +
                    "login_password VARCHAR(50), " +
                    "status VARCHAR(20), " +
                    "joining_date DATE NOT NULL)");
            try {
                stmt.executeUpdate("ALTER TABLE operators ADD COLUMN login_password VARCHAR(50)");
            } catch (Exception ignored) {
                // Column already exists in upgraded databases.
            }

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Database initialization error", ex);
        }
    }
}