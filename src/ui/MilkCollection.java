package ui;

import database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class MilkCollection extends JFrame {

    private JPanel mainPanel;
    private JTextField txtFarmerId;
    private JTextField txtFarmerName;
    private JTextField txtQuantity;
    private JTextField txtFat;
    private JTextField txtSnf;
    private JTextField txtRate;
    private JTextField txtAmount;
    private JTextField txtDate;

    private JComboBox<String> cmbMilkType;
    private JComboBox<String> cmbShift;

    private JButton btnSearch;
    private JButton btnSave;
    private JButton btnClear;
    private JButton btnBack;

    public MilkCollection() {
        this(false);
    }

    public MilkCollection(boolean embedded) {
        setTitle("Milk Collection");
        setSize(980, 680);
        setLocationRelativeTo(null);
        if (!embedded) {
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        JLabel heading = new JLabel("Milk Collection");
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
        form.add(createLabel("Shift"), gbc);

        gbc.gridx = 1;
        cmbShift = createComboBox(new String[]{"Morning", "Evening"});
        form.add(cmbShift, gbc);

        gbc.gridx = 2;
        form.add(createLabel("Milk Type"), gbc);

        gbc.gridx = 3;
        cmbMilkType = createComboBox(new String[]{"Cow", "Buffalo"});
        form.add(cmbMilkType, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(createLabel("Quantity (Ltr)"), gbc);

        gbc.gridx = 1;
        txtQuantity = createTextField();
        form.add(txtQuantity, gbc);

        gbc.gridx = 2;
        form.add(createLabel("FAT %"), gbc);

        gbc.gridx = 3;
        txtFat = createTextField();
        form.add(txtFat, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        form.add(createLabel("SNF %"), gbc);

        gbc.gridx = 1;
        txtSnf = createTextField();
        form.add(txtSnf, gbc);

        gbc.gridx = 2;
        form.add(createLabel("Rate"), gbc);

        gbc.gridx = 3;
        txtRate = createTextField();
        txtRate.setEditable(false);
        form.add(txtRate, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        form.add(createLabel("Amount"), gbc);

        gbc.gridx = 1;
        txtAmount = createTextField();
        txtAmount.setEditable(false);
        form.add(txtAmount, gbc);

        gbc.gridx = 2;
        form.add(createLabel("Collection Date"), gbc);

        gbc.gridx = 3;
        txtDate = createTextField(LocalDate.now().toString());
        form.add(txtDate, gbc);

        mainPanel.add(form, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 12));
        actionPanel.setBackground(new Color(245, 247, 250));
        btnSave = createButton("Save", new Color(39, 174, 96));
        btnClear = createButton("Clear", new Color(149, 165, 166));
        btnBack = createButton("Back", new Color(192, 57, 43));
        actionPanel.add(btnSave);
        actionPanel.add(btnClear);
        actionPanel.add(btnBack);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> searchFarmer());
        btnSave.addActionListener(e -> saveCollection());
        btnClear.addActionListener(e -> clearForm());
        btnBack.addActionListener(e -> closeCurrentTab());

        KeyAdapter rateWatcher = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateRate();
            }
        };

        txtFat.addKeyListener(rateWatcher);
        txtSnf.addKeyListener(rateWatcher);
        txtQuantity.addKeyListener(rateWatcher);

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
        field.setPreferredSize(new Dimension(180, 34));
        return field;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setPreferredSize(new Dimension(180, 34));
        return comboBox;
    }

    private JButton createButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(140, 38));
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

    private void searchFarmer() {
        if (txtFarmerId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Farmer ID");
            return;
        }

        try {
            int farmerId = Integer.parseInt(txtFarmerId.getText().trim());
            String sql = "SELECT farmer_name FROM farmers WHERE farmer_id=?";
            try (Connection con = DBConnection.getConnection()) {
                if (con == null) {
                    JOptionPane.showMessageDialog(this, "Database connection failed.");
                    return;
                }
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, farmerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            txtFarmerName.setText(rs.getString("farmer_name"));
                        } else {
                            JOptionPane.showMessageDialog(this, "Farmer Not Found");
                            txtFarmerName.setText("");
                        }
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Farmer ID must be a number.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void saveCollection() {
        if (txtFarmerId.getText().trim().isEmpty() || txtFarmerName.getText().trim().isEmpty() ||
                txtQuantity.getText().trim().isEmpty() || txtFat.getText().trim().isEmpty() ||
                txtSnf.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.");
            return;
        }

        if (txtRate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Unable to determine rate. Check fat/SNF values.");
            return;
        }

        try {
            int farmerId = Integer.parseInt(txtFarmerId.getText().trim());
            double quantity = Double.parseDouble(txtQuantity.getText().trim());
            double fat = Double.parseDouble(txtFat.getText().trim());
            double snf = Double.parseDouble(txtSnf.getText().trim());
            double rate = Double.parseDouble(txtRate.getText().trim());
            double amount = Double.parseDouble(txtAmount.getText().trim());
            java.sql.Date collectionDate = java.sql.Date.valueOf(txtDate.getText().trim());

            String checkSql = "SELECT COUNT(*) FROM milk_collection WHERE farmer_id=? AND collection_date=? AND milk_type=? AND shift=?";
            String sql = "INSERT INTO milk_collection (farmer_id,shift,milk_type,quantity,fat,snf,rate,total_amount,collection_date) VALUES (?,?,?,?,?,?,?,?,?)";

            try (Connection con = DBConnection.getConnection()) {
                if (con == null) {
                    JOptionPane.showMessageDialog(this, "Database connection failed.");
                    return;
                }

                try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                    checkPs.setInt(1, farmerId);
                    checkPs.setDate(2, collectionDate);
                    checkPs.setString(3, cmbMilkType.getSelectedItem().toString());
                    checkPs.setString(4, cmbShift.getSelectedItem().toString());
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(this, "Milk already collected for this shift.");
                            return;
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, farmerId);
                    ps.setString(2, cmbShift.getSelectedItem().toString());
                    ps.setString(3, cmbMilkType.getSelectedItem().toString());
                    ps.setDouble(4, quantity);
                    ps.setDouble(5, fat);
                    ps.setDouble(6, snf);
                    ps.setDouble(7, rate);
                    ps.setDouble(8, amount);
                    ps.setDate(9, collectionDate);
                    ps.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Milk Collection Saved Successfully");
            clearForm();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid collection date in YYYY-MM-DD format.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void clearForm() {
        txtFarmerId.setText("");
        txtFarmerName.setText("");
        txtQuantity.setText("");
        txtFat.setText("");
        txtSnf.setText("");
        txtRate.setText("");
        txtAmount.setText("");
        txtDate.setText(LocalDate.now().toString());
        cmbShift.setSelectedIndex(0);
        cmbMilkType.setSelectedIndex(0);
        txtFarmerId.requestFocus();
    }

    private void calculateRate() {
        try {
            if (txtFat.getText().isEmpty() || txtSnf.getText().isEmpty() || txtQuantity.getText().isEmpty()) {
                return;
            }

            double fat = Double.parseDouble(txtFat.getText().trim());
            double snf = Double.parseDouble(txtSnf.getText().trim());
            double qty = Double.parseDouble(txtQuantity.getText().trim());

            String sql = "SELECT rate FROM rate_chart WHERE fat=? AND snf=?";
            try (Connection con = DBConnection.getConnection()) {
                if (con == null) {
                    txtRate.setText("");
                    txtAmount.setText("");
                    return;
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setDouble(1, fat);
                    ps.setDouble(2, snf);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            double rate = rs.getDouble("rate");
                            txtRate.setText(String.valueOf(rate));
                            txtAmount.setText(String.format("%.2f", rate * qty));
                        } else {
                            txtRate.setText("");
                            txtAmount.setText("");
                        }
                    }
                }
            }
        } catch (NumberFormatException ex) {
            txtRate.setText("");
            txtAmount.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
