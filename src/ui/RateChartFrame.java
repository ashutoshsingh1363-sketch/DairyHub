package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import database.DBConnection;

public class RateChartFrame extends JFrame {

    private JPanel mainPanel;
    private final DefaultTableModel tableModel;

    public RateChartFrame() {
        this(false);
    }

    public RateChartFrame(boolean embedded) {
        setTitle("DAIRY HUB - Rate Chart");
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

        JLabel title = new JLabel("Rate Chart");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(18, 76, 140));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 232)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JTextField txtFat = new JTextField();
        JTextField txtSnf = new JTextField();
        JTextField txtRate = new JTextField();

        formPanel.add(new JLabel("Fat"));
        formPanel.add(txtFat);
        formPanel.add(new JLabel("SNF"));
        formPanel.add(txtSnf);
        formPanel.add(new JLabel("Rate (Rs./L)"));
        formPanel.add(txtRate);

        JButton btnAdd = new JButton("Add Rate");
        btnAdd.setBackground(new Color(39, 174, 96));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        formPanel.add(btnAdd);

        JButton btnClear = new JButton("Clear");
        btnClear.setBackground(new Color(149, 165, 166));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        formPanel.add(btnClear);

        mainPanel.add(formPanel, BorderLayout.WEST);

        String[] columns = {"Fat", "SNF", "Rate"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        JScrollPane tableScroll = new JScrollPane(table);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> saveRate(txtFat, txtSnf, txtRate));
        btnClear.addActionListener(e -> {
            txtFat.setText("");
            txtSnf.setText("");
            txtRate.setText("");
        });

        loadRatesFromDatabase();
        if (!embedded) {
            setVisible(true);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void saveRate(JTextField txtFat, JTextField txtSnf, JTextField txtRate) {
        try {
            double fat = Double.parseDouble(txtFat.getText().trim());
            double snf = Double.parseDouble(txtSnf.getText().trim());
            double rate = Double.parseDouble(txtRate.getText().trim());

            if (fat <= 0 || snf <= 0 || rate <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid positive values.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                if (con == null) {
                    JOptionPane.showMessageDialog(this, "Database connection failed.");
                    return;
                }

                try (Statement st = con.createStatement()) {
                    st.executeUpdate("CREATE TABLE IF NOT EXISTS rate_chart (id INT AUTO_INCREMENT PRIMARY KEY, fat DOUBLE NOT NULL, snf DOUBLE NOT NULL, rate DOUBLE NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, UNIQUE KEY uq_rate_chart (fat, snf))");
                }

                String sql = "INSERT INTO rate_chart (fat, snf, rate) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE rate = VALUES(rate)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setDouble(1, fat);
                    ps.setDouble(2, snf);
                    ps.setDouble(3, rate);
                    ps.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Rate saved successfully.");
            txtFat.setText("");
            txtSnf.setText("");
            txtRate.setText("");
            loadRatesFromDatabase();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numeric values.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void loadRatesFromDatabase() {
        tableModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return;
            }

            try (Statement st = con.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS rate_chart (id INT AUTO_INCREMENT PRIMARY KEY, fat DOUBLE NOT NULL, snf DOUBLE NOT NULL, rate DOUBLE NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, UNIQUE KEY uq_rate_chart (fat, snf))");
            }

            String sql = "SELECT fat, snf, rate FROM rate_chart ORDER BY fat, snf";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{rs.getDouble("fat"), rs.getDouble("snf"), rs.getDouble("rate")});
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
