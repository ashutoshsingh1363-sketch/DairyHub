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

public class BillFrame extends JFrame {

    private JPanel mainPanel;
    private final DefaultTableModel tableModel;

    public BillFrame() {
        this(false);
    }

    public BillFrame(boolean embedded) {
        setTitle("DAIRY HUB - Bills");
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

        JLabel title = new JLabel("Bills");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(18, 76, 140));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 232)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JTextField txtFarmer = new JTextField();
        JTextField txtAmount = new JTextField();
        JTextField txtDueDate = new JTextField(LocalDate.now().toString());
        JTextArea txtRemarks = new JTextArea(4, 20);

        formPanel.add(new JLabel("Farmer Name"));
        formPanel.add(txtFarmer);
        formPanel.add(new JLabel("Amount (Rs.)"));
        formPanel.add(txtAmount);
        formPanel.add(new JLabel("Due Date"));
        formPanel.add(txtDueDate);
        formPanel.add(new JLabel("Remarks"));
        formPanel.add(new JScrollPane(txtRemarks));

        JButton btnGenerate = new JButton("Generate Bill");
        btnGenerate.setBackground(new Color(39, 174, 96));
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFocusPainted(false);
        formPanel.add(btnGenerate);

        JButton btnClear = new JButton("Clear");
        btnClear.setBackground(new Color(149, 165, 166));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        formPanel.add(btnClear);

        mainPanel.add(formPanel, BorderLayout.WEST);

        String[] columns = {"Farmer", "Amount", "Due Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> {
            String farmer = txtFarmer.getText().trim();
            String amountStr = txtAmount.getText().trim();
            String dueDateStr = txtDueDate.getText().trim();
            String remarks = txtRemarks.getText().trim();

            if (farmer.isEmpty() || amountStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter farmer and amount.");
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                java.sql.Date.valueOf(dueDateStr); // validate date format

                try (Connection con = DBConnection.getConnection()) {
                    if (con == null) {
                        JOptionPane.showMessageDialog(this, "Database connection failed.");
                        return;
                    }

                    ensureTableExists(con);

                    String sql = "INSERT INTO bills (farmer_name, amount, due_date, remarks, status) VALUES (?, ?, ?, ?, 'Pending')";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, farmer);
                        ps.setDouble(2, amount);
                        ps.setDate(3, java.sql.Date.valueOf(dueDateStr));
                        ps.setString(4, remarks);
                        ps.executeUpdate();
                    }
                }

                JOptionPane.showMessageDialog(this, "Bill generated successfully.");
                txtFarmer.setText("");
                txtAmount.setText("");
                txtDueDate.setText(LocalDate.now().toString());
                txtRemarks.setText("");
                loadBillsFromDatabase();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid amount.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Due Date must be in YYYY-MM-DD format.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving bill: " + ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            txtFarmer.setText("");
            txtAmount.setText("");
            txtDueDate.setText(LocalDate.now().toString());
            txtRemarks.setText("");
        });

        loadBillsFromDatabase();

        if (!embedded) {
            setVisible(true);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void ensureTableExists(Connection con) throws Exception {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS bills (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "farmer_name VARCHAR(100) NOT NULL, " +
                    "amount DOUBLE NOT NULL, " +
                    "due_date DATE NOT NULL, " +
                    "remarks TEXT, " +
                    "status VARCHAR(20) DEFAULT 'Pending', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    private void loadBillsFromDatabase() {
        tableModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return;
            }

            ensureTableExists(con);

            String sql = "SELECT farmer_name, amount, due_date, status FROM bills ORDER BY id DESC";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getString("farmer_name"),
                            "Rs. " + String.format("%.2f", rs.getDouble("amount")),
                            rs.getDate("due_date").toString(),
                            rs.getString("status")
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
