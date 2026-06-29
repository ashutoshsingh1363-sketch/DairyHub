package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import database.DBConnection;

public class FeedManagementFrame extends JFrame {

    private JPanel mainPanel;
    private final DefaultTableModel tableModel;

    public FeedManagementFrame() {
        this(false);
    }

    public FeedManagementFrame(boolean embedded) {
        setTitle("DAIRY HUB - Feed Management");
        setSize(900, 520);
        setLocationRelativeTo(null);
        if (!embedded) {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(new Color(245, 247, 250));
        if (!embedded) {
            setContentPane(mainPanel);
        }

        JLabel title = new JLabel("Feed Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(18, 76, 140));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 232)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JTextField txtFeedName = new JTextField();
        JTextField txtQty = new JTextField();
        JTextField txtPrice = new JTextField();
        JTextField txtSupplier = new JTextField();

        formPanel.add(new JLabel("Feed Name"));
        formPanel.add(txtFeedName);
        formPanel.add(new JLabel("Quantity (Kg)"));
        formPanel.add(txtQty);
        formPanel.add(new JLabel("Price (Rs./Kg)"));
        formPanel.add(txtPrice);
        formPanel.add(new JLabel("Supplier"));
        formPanel.add(txtSupplier);

        JButton btnSave = new JButton("Save Feed");
        btnSave.setBackground(new Color(39, 174, 96));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        formPanel.add(btnSave);

        JButton btnClear = new JButton("Clear");
        btnClear.setBackground(new Color(149, 165, 166));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        formPanel.add(btnClear);

        mainPanel.add(formPanel, BorderLayout.WEST);

        String[] columns = {"Feed Name", "Quantity", "Price", "Supplier", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        btnSave.addActionListener(e -> {
            String feedName = txtFeedName.getText().trim();
            String qtyStr = txtQty.getText().trim();
            String priceStr = txtPrice.getText().trim();
            String supplier = txtSupplier.getText().trim();

            if (feedName.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter feed details.");
                return;
            }

            try {
                double qty = Double.parseDouble(qtyStr);
                double price = Double.parseDouble(priceStr);
                java.sql.Date purchaseDate = java.sql.Date.valueOf(LocalDate.now());

                try (Connection con = DBConnection.getConnection()) {
                    if (con == null) {
                        JOptionPane.showMessageDialog(this, "Database connection failed.");
                        return;
                    }

                    ensureTableExists(con);

                    String sql = "INSERT INTO feeds (feed_name, quantity, price, supplier, purchase_date) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, feedName);
                        ps.setDouble(2, qty);
                        ps.setDouble(3, price);
                        ps.setString(4, supplier);
                        ps.setDate(5, purchaseDate);
                        ps.executeUpdate();
                    }
                }

                JOptionPane.showMessageDialog(this, "Feed saved successfully.");
                txtFeedName.setText("");
                txtQty.setText("");
                txtPrice.setText("");
                txtSupplier.setText("");
                loadFeedsFromDatabase();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Quantity and Price must be valid numbers.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving feed: " + ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            txtFeedName.setText("");
            txtQty.setText("");
            txtPrice.setText("");
            txtSupplier.setText("");
        });

        loadFeedsFromDatabase();

        if (!embedded) {
            setVisible(true);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void ensureTableExists(Connection con) throws Exception {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS feeds (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "feed_name VARCHAR(100) NOT NULL, " +
                    "quantity DOUBLE NOT NULL, " +
                    "price DOUBLE NOT NULL, " +
                    "supplier VARCHAR(100), " +
                    "purchase_date DATE NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    private void loadFeedsFromDatabase() {
        tableModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return;
            }

            ensureTableExists(con);

            String sql = "SELECT feed_name, quantity, price, supplier, purchase_date FROM feeds ORDER BY id DESC";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getString("feed_name"),
                            rs.getDouble("quantity"),
                            "Rs. " + String.format("%.2f", rs.getDouble("price")),
                            rs.getString("supplier"),
                            rs.getDate("purchase_date").toString()
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
