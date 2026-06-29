package ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DBConnection;
public class PaymentFrame extends JFrame {

    private JPanel mainPanel;
    private JTextField txtFarmerId;
    private JTextField txtFarmerName;
    private JTextField txtFromDate;
    private JTextField txtToDate;
    private JTextField txtTotalMilk;
    private JTextField txtTotalAmount;
    private JTextArea txtRemarks;
    private JButton btnSearch;
    private JButton btnSave;
    private JButton btnPrint;
    private JButton btnClear;
    private JButton btnBack;

    public PaymentFrame() {
        this(false);
    }

    public PaymentFrame(boolean embedded) {
        setTitle("Milk Payment");
        setSize(980, 680);
        setLocationRelativeTo(null);
        if (!embedded) {
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        if (!embedded) {
            setContentPane(mainPanel);
        }

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(18, 76, 140));
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel heading = new JLabel("Milk Payment");
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.add(heading, BorderLayout.WEST);
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(createLabel("Farmer ID"), gbc);

        gbc.gridx = 1;
        txtFarmerId = createTextField();
        form.add(txtFarmerId, gbc);

        gbc.gridx = 2;
        btnSearch = createButton("Search", new Color(52, 152, 219));
        form.add(btnSearch, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(createLabel("Farmer Name"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtFarmerName = createTextField();
        txtFarmerName.setEditable(false);
        form.add(txtFarmerName, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(createLabel("From Date"), gbc);

        gbc.gridx = 1;
        txtFromDate = createTextField(LocalDate.now().withDayOfMonth(1).toString());
        form.add(txtFromDate, gbc);

        gbc.gridx = 2;
        form.add(createLabel("To Date"), gbc);

        gbc.gridx = 3;
        txtToDate = createTextField(LocalDate.now().toString());
        form.add(txtToDate, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(createLabel("Total Milk"), gbc);

        gbc.gridx = 1;
        txtTotalMilk = createTextField();
        txtTotalMilk.setEditable(false);
        form.add(txtTotalMilk, gbc);

        gbc.gridx = 2;
        form.add(createLabel("Total Amount"), gbc);

        gbc.gridx = 3;
        txtTotalAmount = createTextField();
        txtTotalAmount.setEditable(false);
        form.add(txtTotalAmount, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(createLabel("Remarks"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        txtRemarks = new JTextArea(5, 30);
        txtRemarks.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        form.add(new JScrollPane(txtRemarks), gbc);

        mainPanel.add(form, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 12));
        actionPanel.setBackground(new Color(245, 247, 250));
        btnSave = createButton("Save Payment", new Color(39, 174, 96));
        btnPrint = createButton("Print Receipt", new Color(41, 128, 185));
        btnClear = createButton("Clear", new Color(149, 165, 166));
        btnBack = createButton("Back", new Color(192, 57, 43));
        actionPanel.add(btnSave);
        actionPanel.add(btnPrint);
        actionPanel.add(btnClear);
        actionPanel.add(btnBack);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> searchPayment());
        btnSave.addActionListener(e -> savePayment());
        btnPrint.addActionListener(e -> openReceipt());
        btnClear.addActionListener(e -> clearPaymentForm());
        btnBack.addActionListener(e -> closeCurrentTab());

        if (!embedded) {
            setVisible(true);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }

    private JTextField createTextField() {
        return createTextField("");
    }

    private JTextField createTextField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(180, 32));
        return field;
    }

    private JButton createButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bg);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 38));
        return button;
    }

    private void closeCurrentTab() {
        Container parent = mainPanel.getParent();
        while (parent != null && !(parent instanceof JTabbedPane)) {
            parent = parent.getParent();
        }
        if (parent instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) parent;
            int tabIndex = tabs.indexOfComponent(mainPanel);
            if (tabIndex != -1) {
                tabs.removeTabAt(tabIndex);
                return;
            }
        }
        dispose();
    }

    private void searchPayment() {
        if (txtFarmerId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Farmer ID");
            return;
        }

        try {
            int farmerId = Integer.parseInt(txtFarmerId.getText().trim());
            java.sql.Date fromDate = java.sql.Date.valueOf(txtFromDate.getText().trim());
            java.sql.Date toDate = java.sql.Date.valueOf(txtToDate.getText().trim());

            String farmerSql = "SELECT farmer_name FROM farmers WHERE farmer_id=?";
            String paymentSql = "SELECT IFNULL(SUM(quantity),0) AS total_milk, IFNULL(SUM(total_amount),0) AS total_amount " +
                    "FROM milk_collection WHERE farmer_id=? AND collection_date BETWEEN ? AND ?";

            try (Connection con = DBConnection.getConnection()) {
                if (con == null) {
                    JOptionPane.showMessageDialog(this, "Database connection failed.");
                    return;
                }

                try (PreparedStatement farmerPs = con.prepareStatement(farmerSql)) {
                    farmerPs.setInt(1, farmerId);
                    try (ResultSet farmerRs = farmerPs.executeQuery()) {
                        if (farmerRs.next()) {
                            txtFarmerName.setText(farmerRs.getString("farmer_name"));
                        } else {
                            JOptionPane.showMessageDialog(this, "Farmer Not Found");
                            txtFarmerName.setText("");
                            txtTotalMilk.setText("");
                            txtTotalAmount.setText("");
                            return;
                        }
                    }
                }

                try (PreparedStatement paymentPs = con.prepareStatement(paymentSql)) {
                    paymentPs.setInt(1, farmerId);
                    paymentPs.setDate(2, fromDate);
                    paymentPs.setDate(3, toDate);
                    try (ResultSet paymentRs = paymentPs.executeQuery()) {
                        if (paymentRs.next()) {
                            txtTotalMilk.setText(String.valueOf(paymentRs.getDouble("total_milk")));
                            txtTotalAmount.setText(String.format("%.2f", paymentRs.getDouble("total_amount")));
                        }
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Farmer ID must be a number.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid dates in YYYY-MM-DD format.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void savePayment() {
        if (txtFarmerId.getText().trim().isEmpty() || txtFarmerName.getText().trim().isEmpty() ||
                txtTotalMilk.getText().trim().isEmpty() || txtTotalAmount.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please search payment details first.");
            return;
        }

        try {
            int farmerId = Integer.parseInt(txtFarmerId.getText().trim());
            double totalAmount = Double.parseDouble(txtTotalAmount.getText().trim());
            java.sql.Date fromDate = java.sql.Date.valueOf(txtFromDate.getText().trim());
            java.sql.Date toDate = java.sql.Date.valueOf(txtToDate.getText().trim());

            if (totalAmount <= 0) {
                JOptionPane.showMessageDialog(this, "Payment amount should be greater than zero.");
                return;
            }

            String checkSql = "SELECT COUNT(*) FROM payments WHERE farmer_id=? AND from_date=? AND to_date=?";
            String sql = "INSERT INTO payments(farmer_id,from_date,to_date,total_milk,total_amount,payment_date,remarks) VALUES(?,?,?,?,?,?,?)";

            try (Connection con = DBConnection.getConnection()) {
                if (con == null) {
                    JOptionPane.showMessageDialog(this, "Database connection failed.");
                    return;
                }

                try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                    checkPs.setInt(1, farmerId);
                    checkPs.setDate(2, fromDate);
                    checkPs.setDate(3, toDate);
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(this, "Payment already saved for this period.");
                            return;
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, farmerId);
                    ps.setDate(2, fromDate);
                    ps.setDate(3, toDate);
                    ps.setDouble(4, Double.parseDouble(txtTotalMilk.getText().trim()));
                    ps.setDouble(5, totalAmount);
                    ps.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
                    ps.setString(7, txtRemarks.getText());
                    ps.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(this, "Payment Saved Successfully");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numeric values for ID and amounts.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid dates in YYYY-MM-DD format.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void openReceipt() {
        if (txtFarmerId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search payment first.");
            return;
        }
        ReceiptFrame receipt = new ReceiptFrame(
                txtFarmerId.getText(),
                txtFarmerName.getText(),
                txtFromDate.getText(),
                txtToDate.getText(),
                txtTotalMilk.getText(),
                txtTotalAmount.getText(),
                txtRemarks.getText());
        receipt.setVisible(true);
    }

    private void clearPaymentForm() {
        txtFarmerId.setText("");
        txtFarmerName.setText("");
        txtFromDate.setText(LocalDate.now().withDayOfMonth(1).toString());
        txtToDate.setText(LocalDate.now().toString());
        txtTotalMilk.setText("");
        txtTotalAmount.setText("");
        txtRemarks.setText("");
        txtFarmerId.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PaymentFrame::new);
    }
}
