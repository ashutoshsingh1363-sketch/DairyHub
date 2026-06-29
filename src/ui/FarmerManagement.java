package ui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class FarmerManagement extends JFrame {
    private JPanel mainPanel;
    private JTextField txtId, txtName, txtFather, txtDob, txtMobile, txtAltMobile;
    private JTextField txtVillage, txtPost, txtBlock, txtDistrict, txtState, txtPin;
    private JTextField txtBankName, txtBranch, txtIfsc, txtAccountNo, txtAccountHolder;
    private JTextField txtAadhaar, txtPan, txtCenterName, txtJoiningDate, txtSearchFilter;
    private JComboBox<String> cmbGender, cmbMilkType, cmbStatus;
    private JLabel lblPhoto;
    private JTable farmerTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalFarmers;
    private JButton btnShowAddForm;
    private JButton btnHeaderBack;
    private JButton btnClose;
    private JPanel cardsPanel;
    private CardLayout contentCard;
    private String selectedPhotoPath = "";
    private final Set<String> farmerColumns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    private final Color primary = new Color(26, 82, 118);
    private final Color bg = new Color(244, 247, 250);
    private final Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);

    public FarmerManagement() {
        this(false);
    }

    public FarmerManagement(boolean embedded) {
        setTitle("Farmer Management V2");
        setSize(1180, 760);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        if (!embedded) {
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

        mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(bg);
        if (!embedded) {
            setContentPane(mainPanel);
        }

        mainPanel.add(createHeader(), BorderLayout.NORTH);

        cardsPanel = new JPanel(contentCard = new CardLayout());
        cardsPanel.setOpaque(false);
        cardsPanel.add(createTablePanel(), "TABLE");
        cardsPanel.add(createFormPanel(), "FORM");

        JScrollPane scrollBody = new JScrollPane(cardsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollBody.setBorder(BorderFactory.createEmptyBorder());
        scrollBody.getVerticalScrollBar().setUnitIncrement(14);
        mainPanel.add(scrollBody, BorderLayout.CENTER);

        loadFarmerColumns();
        wireEvents();
        autoGenerateFarmerId();
        loadFarmerTable();
        showTable();

        if (!embedded) {
            setVisible(true);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("FARMER MANAGEMENT V2");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel sub = new JLabel("Dairy Hub - Farmer profile, bank, identity and dairy details");
        sub.setForeground(new Color(225, 235, 245));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(sub);

        header.add(titleBox, BorderLayout.WEST);

        btnHeaderBack = new JButton("Back");
        btnHeaderBack.setBackground(new Color(245, 247, 250));
        btnHeaderBack.setForeground(primary);
        btnHeaderBack.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHeaderBack.setFocusPainted(false);
        btnHeaderBack.setPreferredSize(new Dimension(100, 34));
        btnHeaderBack.addActionListener(e -> showTable());
        btnHeaderBack.setVisible(false);

        btnShowAddForm = new JButton("Add Farmer");
        btnShowAddForm.setBackground(new Color(245, 247, 250));
        btnShowAddForm.setForeground(primary);
        btnShowAddForm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnShowAddForm.setFocusPainted(false);
        btnShowAddForm.setPreferredSize(new Dimension(120, 34));
        btnShowAddForm.addActionListener(e -> {
            clearFields();
            showForm();
        });

        JLabel logoLabel = new JLabel();
        String logoPath = "C:\\Users\\sadhu\\Downloads\\dairy hub logo.png";
        File logoFile = new File(logoPath);
        if (logoFile.exists()) {
            ImageIcon icon = new ImageIcon(logoPath);
            Image img = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(img));
        } else {
            logoLabel.setText("Logo");
            logoLabel.setForeground(Color.WHITE);
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        }
        logoLabel.setPreferredSize(new Dimension(80, 80));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(btnHeaderBack);
        rightPanel.add(btnShowAddForm);
        rightPanel.add(logoLabel);

        btnClose = actionButton("Back", new Color(160, 174, 192), Color.WHITE);
        btnClose.addActionListener(e -> closeCurrentTab());
        rightPanel.add(btnClose);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
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

    private void showForm() {
        if (contentCard != null && cardsPanel != null) {
            contentCard.show(cardsPanel, "FORM");
            if (btnHeaderBack != null) {
                btnHeaderBack.setVisible(true);
            }
            if (btnShowAddForm != null) {
                btnShowAddForm.setVisible(false);
            }
        }
    }

    private void showTable() {
        if (contentCard != null && cardsPanel != null) {
            contentCard.show(cardsPanel, "TABLE");
            if (farmerTable != null) {
                farmerTable.clearSelection();
            }
            if (btnHeaderBack != null) {
                btnHeaderBack.setVisible(false);
            }
            if (btnShowAddForm != null) {
                btnShowAddForm.setVisible(true);
            }
        }
    }

    private JPanel createFormPanel() {
        JPanel formWrap = new JPanel(new BorderLayout(12, 12));
        formWrap.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout(10, 10));
        left.setOpaque(false);
        left.add(createPhotoPanel(), BorderLayout.WEST);

        JPanel details = new JPanel(new GridLayout(2, 2, 10, 10));
        details.setOpaque(false);
        details.add(createPersonalPanel());
        details.add(createAddressPanel());
        details.add(createBankPanel());
        details.add(createDairyPanel());
        left.add(details, BorderLayout.CENTER);

        formWrap.add(left, BorderLayout.CENTER);
        formWrap.add(createButtonPanel(), BorderLayout.SOUTH);
        return formWrap;
    }

    private JPanel createPhotoPanel() {
        JPanel panel = cardPanel("Photo");
        panel.setPreferredSize(new Dimension(160, 260));
        panel.setLayout(new BorderLayout(8, 8));

        lblPhoto = new JLabel("No Photo", SwingConstants.CENTER);
        lblPhoto.setOpaque(true);
        lblPhoto.setBackground(new Color(232, 238, 244));
        lblPhoto.setForeground(new Color(80, 90, 100));
        lblPhoto.setBorder(BorderFactory.createLineBorder(new Color(210, 218, 226)));

        JButton btnPhoto = actionButton("Upload", new Color(74, 144, 226), Color.WHITE);
        btnPhoto.addActionListener(e -> choosePhoto());

        panel.add(lblPhoto, BorderLayout.CENTER);
        panel.add(btnPhoto, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPersonalPanel() {
        JPanel panel = cardPanel("Personal Details");
        panel.setLayout(new GridBagLayout());

        txtId = textField();
        txtName = textField();
        txtFather = textField();
        cmbGender = combo("Male", "Female", "Other");
        txtDob = textField();
        txtMobile = textField();
        txtAltMobile = textField();

        addField(panel, 0, "Farmer ID", txtId);
        addField(panel, 1, "Farmer Name", txtName);
        addField(panel, 2, "Father Name", txtFather);
        addField(panel, 3, "Gender", cmbGender);
        addField(panel, 4, "DOB YYYY-MM-DD", txtDob);
        addField(panel, 5, "Mobile", txtMobile);
        addField(panel, 6, "Alt Mobile", txtAltMobile);
        return panel;
    }

    private JPanel createAddressPanel() {
        JPanel panel = cardPanel("Address");
        panel.setLayout(new GridBagLayout());

        txtVillage = textField();
        txtPost = textField();
        txtBlock = textField();
        txtDistrict = textField();
        txtState = textField();
        txtPin = textField();

        addField(panel, 0, "Village", txtVillage);
        addField(panel, 1, "Post", txtPost);
        addField(panel, 2, "Block", txtBlock);
        addField(panel, 3, "District", txtDistrict);
        addField(panel, 4, "State", txtState);
        addField(panel, 5, "PIN Code", txtPin);
        return panel;
    }

    private JPanel createBankPanel() {
        JPanel panel = cardPanel("Bank and Identity");
        panel.setLayout(new GridBagLayout());

        txtBankName = textField();
        txtBranch = textField();
        txtIfsc = textField();
        txtAccountNo = textField();
        txtAccountHolder = textField();
        txtAadhaar = textField();
        txtPan = textField();

        addField(panel, 0, "Bank Name", txtBankName);
        addField(panel, 1, "Branch", txtBranch);
        addField(panel, 2, "IFSC", txtIfsc);
        addField(panel, 3, "Account No.", txtAccountNo);
        addField(panel, 4, "Holder Name", txtAccountHolder);
        addField(panel, 5, "Aadhaar", txtAadhaar);
        addField(panel, 6, "PAN", txtPan);
        return panel;
    }

    private JPanel createDairyPanel() {
        JPanel panel = cardPanel("Dairy Details");
        panel.setLayout(new GridBagLayout());

        cmbMilkType = combo("Cow", "Buffalo", "Both");
        txtCenterName = textField();
        txtJoiningDate = textField();
        txtJoiningDate.setText(LocalDate.now().toString());
        cmbStatus = combo("Active", "Inactive");

        addField(panel, 0, "Milk Type", cmbMilkType);
        addField(panel, 1, "Center Name", txtCenterName);
        addField(panel, 2, "Joining Date", txtJoiningDate);
        addField(panel, 3, "Status", cmbStatus);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);

        JButton btnSave = actionButton("Save", new Color(39, 174, 96), Color.BLACK);
        JButton btnUpdate = actionButton("Update", new Color(41, 128, 185), Color.BLACK);
        JButton btnDelete = actionButton("Delete", new Color(192, 57, 43), Color.BLACK);
        JButton btnSearch = actionButton("Search ID", new Color(125, 60, 152), Color.BLACK);
        JButton btnPrint = actionButton("Print Card", new Color(230, 126, 34), Color.BLACK);
        JButton btnClear = actionButton("Clear", new Color(213, 219, 219), Color.BLACK);
        JButton btnBack = actionButton("Back to List", new Color(120, 130, 140), Color.BLACK);

        btnSave.addActionListener(e -> saveFarmer());
        btnUpdate.addActionListener(e -> updateFarmer());
        btnDelete.addActionListener(e -> deleteFarmer());
        btnSearch.addActionListener(e -> searchFarmerById());
        btnPrint.addActionListener(e -> printFarmerCard());
        btnClear.addActionListener(e -> clearFields());
        btnBack.addActionListener(e -> showTable());

        panel.add(btnSave);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnSearch);
        panel.add(btnPrint);
        panel.add(btnClear);
        panel.add(btnBack);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = cardPanel("Farmer List");
        panel.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:"));

        txtSearchFilter = new JTextField(20);
        txtSearchFilter.setFont(inputFont);
        txtSearchFilter.setToolTipText("Search by ID, name, mobile or village");
        searchPanel.add(txtSearchFilter);

        lblTotalFarmers = new JLabel("Total Farmers: 0");
        lblTotalFarmers.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalFarmers.setForeground(new Color(60, 75, 95));
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(lblTotalFarmers, BorderLayout.EAST);

        String[] columns = {"ID", "Name", "Mobile", "Village", "Milk Type", "Center", "Status", "Joining Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        farmerTable = new JTable(tableModel);
        farmerTable.setRowHeight(26);
        farmerTable.setFont(inputFont);
        farmerTable.setSelectionBackground(new Color(220, 238, 252));
        farmerTable.setSelectionForeground(Color.BLACK);

        JTableHeader header = farmerTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(235, 240, 245));
        header.setForeground(new Color(45, 55, 72));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(farmerTable), BorderLayout.CENTER);
        return panel;
    }

    private void wireEvents() {
        farmerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && farmerTable.getSelectedRow() >= 0) {
                int row = farmerTable.getSelectedRow();
                txtId.setText(String.valueOf(tableModel.getValueAt(row, 0)));
                searchFarmerById();
                showForm();
            }
        });

        txtSearchFilter.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadFarmerTable(); }
            public void removeUpdate(DocumentEvent e) { loadFarmerTable(); }
            public void changedUpdate(DocumentEvent e) { loadFarmerTable(); }
        });
    }

    private void saveFarmer() {
        if (!validateForm()) {
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                showMessage("Database connection failed.");
                return;
            }

            if (farmerExists(con, Integer.parseInt(txtId.getText().trim()))) {
                showMessage("Farmer ID already exists. Use Update.");
                return;
            }

            Map<String, Object> data = collectFarmerData();
            StringBuilder cols = new StringBuilder();
            StringBuilder marks = new StringBuilder();

            for (String col : data.keySet()) {
                if (cols.length() > 0) {
                    cols.append(", ");
                    marks.append(", ");
                }
                cols.append(col);
                marks.append("?");
            }

            String sql = "INSERT INTO farmers(" + cols + ") VALUES(" + marks + ")";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                bindValues(ps, data);
                ps.executeUpdate();
            }

            showMessage("Farmer saved successfully.");
            loadFarmerTable();
            clearFields();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void updateFarmer() {
        if (!validateForm()) {
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                showMessage("Database connection failed.");
                return;
            }

            int farmerId = Integer.parseInt(txtId.getText().trim());
            if (!farmerExists(con, farmerId)) {
                showMessage("Farmer ID not found.");
                return;
            }

            Map<String, Object> data = collectFarmerData();
            data.remove("farmer_id");

            StringBuilder setSql = new StringBuilder();
            for (String col : data.keySet()) {
                if (setSql.length() > 0) {
                    setSql.append(", ");
                }
                setSql.append(col).append("=?");
            }

            String sql = "UPDATE farmers SET " + setSql + " WHERE farmer_id=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int index = bindValues(ps, data);
                ps.setInt(index, farmerId);
                ps.executeUpdate();
            }

            showMessage("Farmer updated successfully.");
            loadFarmerTable();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteFarmer() {
        if (txtId.getText().trim().isEmpty()) {
            showMessage("Enter Farmer ID.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(this, "Delete this farmer?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                showMessage("Database connection failed.");
                return;
            }

            String sql = "DELETE FROM farmers WHERE farmer_id=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(txtId.getText().trim()));
                int rows = ps.executeUpdate();
                showMessage(rows > 0 ? "Farmer deleted successfully." : "Farmer ID not found.");
            }

            loadFarmerTable();
            clearFields();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void searchFarmerById() {
        if (txtId.getText().trim().isEmpty()) {
            showMessage("Enter Farmer ID.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                showMessage("Database connection failed.");
                return;
            }

            String sql = "SELECT * FROM farmers WHERE farmer_id=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(txtId.getText().trim()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        loadFromResultSet(rs);
                    } else {
                        showMessage("Farmer not found.");
                    }
                }
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadFarmerTable() {
        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return;
            }

            if (farmerColumns.isEmpty()) {
                loadFarmerColumns(con);
            }

            String filter = txtSearchFilter == null ? "" : txtSearchFilter.getText().trim();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT farmer_id, farmer_name, mobile, village");
            sql.append(selectColumn("milk_type"));
            sql.append(selectColumn("center_name"));
            sql.append(selectColumn("status"));
            sql.append(selectColumn("joining_date"));
            sql.append(" FROM farmers");

            boolean hasFilter = !filter.isEmpty();
            if (hasFilter) {
                sql.append(" WHERE CAST(farmer_id AS CHAR) LIKE ? OR farmer_name LIKE ? OR mobile LIKE ? OR village LIKE ?");
            }
            sql.append(" ORDER BY farmer_id DESC");

            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                if (hasFilter) {
                    String like = "%" + filter + "%";
                    ps.setString(1, like);
                    ps.setString(2, like);
                    ps.setString(3, like);
                    ps.setString(4, like);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                                rs.getInt("farmer_id"),
                                rs.getString("farmer_name"),
                                rs.getString("mobile"),
                                rs.getString("village"),
                                rs.getString("milk_type"),
                                rs.getString("center_name"),
                                rs.getString("status"),
                                rs.getString("joining_date")
                        });
                    }
                    if (lblTotalFarmers != null) {
                        lblTotalFarmers.setText("Total Farmers: " + tableModel.getRowCount());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void autoGenerateFarmerId() {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT IFNULL(MAX(farmer_id),0)+1 FROM farmers"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtId.setText(String.valueOf(rs.getInt(1)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void printFarmerCard() {
        if (txtId.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) {
            showMessage("Search or save farmer first.");
            return;
        }

        JTextArea card = new JTextArea();
        card.setFont(new Font("Monospaced", Font.PLAIN, 13));
        card.setText(
                "========================================\n" +
                "              DAIRY HUB\n" +
                "            FARMER CARD\n" +
                "========================================\n\n" +
                "Farmer ID     : " + txtId.getText() + "\n" +
                "Name          : " + txtName.getText() + "\n" +
                "Father Name   : " + txtFather.getText() + "\n" +
                "Mobile        : " + txtMobile.getText() + "\n" +
                "Village       : " + txtVillage.getText() + "\n" +
                "Milk Type     : " + cmbMilkType.getSelectedItem() + "\n" +
                "Center        : " + txtCenterName.getText() + "\n" +
                "Status        : " + cmbStatus.getSelectedItem() + "\n\n" +
                "========================================\n"
        );

        try {
            card.print();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtFather.setText("");
        txtDob.setText("");
        txtMobile.setText("");
        txtAltMobile.setText("");
        txtVillage.setText("");
        txtPost.setText("");
        txtBlock.setText("");
        txtDistrict.setText("");
        txtState.setText("");
        txtPin.setText("");
        txtBankName.setText("");
        txtBranch.setText("");
        txtIfsc.setText("");
        txtAccountNo.setText("");
        txtAccountHolder.setText("");
        txtAadhaar.setText("");
        txtPan.setText("");
        txtCenterName.setText("");
        txtJoiningDate.setText(LocalDate.now().toString());
        cmbGender.setSelectedIndex(0);
        cmbMilkType.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        selectedPhotoPath = "";
        lblPhoto.setIcon(null);
        lblPhoto.setText("No Photo");
        autoGenerateFarmerId();
        txtName.requestFocus();
    }

    private boolean validateForm() {
        if (txtId.getText().trim().isEmpty() || txtName.getText().trim().isEmpty() || txtMobile.getText().trim().isEmpty() || txtVillage.getText().trim().isEmpty()) {
            showMessage("Farmer ID, Name, Mobile and Village are required.");
            return false;
        }

        try {
            Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException ex) {
            showMessage("Farmer ID must be a number.");
            return false;
        }

        if (!txtDob.getText().trim().isEmpty() && !isValidDate(txtDob.getText().trim())) {
            showMessage("DOB must be in YYYY-MM-DD format.");
            return false;
        }

        if (!txtJoiningDate.getText().trim().isEmpty() && !isValidDate(txtJoiningDate.getText().trim())) {
            showMessage("Joining Date must be in YYYY-MM-DD format.");
            return false;
        }

        return true;
    }

    private boolean isValidDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private Map<String, Object> collectFarmerData() {
        Map<String, Object> data = new LinkedHashMap<>();
        put(data, "farmer_id", Integer.parseInt(txtId.getText().trim()));
        put(data, "farmer_name", txtName.getText().trim());
        put(data, "father_name", txtFather.getText().trim());
        put(data, "gender", selected(cmbGender));
        put(data, "dob", txtDob.getText().trim());
        put(data, "mobile", txtMobile.getText().trim());
        put(data, "alternate_mobile", txtAltMobile.getText().trim());
        put(data, "village", txtVillage.getText().trim());
        put(data, "post", txtPost.getText().trim());
        put(data, "block", txtBlock.getText().trim());
        put(data, "district", txtDistrict.getText().trim());
        put(data, "state", txtState.getText().trim());
        put(data, "pin_code", txtPin.getText().trim());
        put(data, "bank_name", txtBankName.getText().trim());
        put(data, "branch", txtBranch.getText().trim());
        put(data, "ifsc", txtIfsc.getText().trim());
        put(data, "account_no", txtAccountNo.getText().trim());
        put(data, "bank_account", txtAccountNo.getText().trim());
        put(data, "account_holder", txtAccountHolder.getText().trim());
        put(data, "aadhaar", txtAadhaar.getText().trim());
        put(data, "pan", txtPan.getText().trim());
        put(data, "milk_type", selected(cmbMilkType));
        put(data, "center_name", txtCenterName.getText().trim());
        put(data, "joining_date", txtJoiningDate.getText().trim());
        put(data, "status", selected(cmbStatus));
        put(data, "photo_path", selectedPhotoPath);
        return data;
    }

    private void put(Map<String, Object> data, String column, Object value) {
        if (farmerColumns.contains(column)) {
            data.put(column, value);
        }
    }

    private int bindValues(PreparedStatement ps, Map<String, Object> data) throws Exception {
        int index = 1;
        for (Object value : data.values()) {
            ps.setObject(index++, value);
        }
        return index;
    }

    private boolean farmerExists(Connection con, int farmerId) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM farmers WHERE farmer_id=?")) {
            ps.setInt(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void loadFromResultSet(ResultSet rs) throws Exception {
        txtId.setText(value(rs, "farmer_id"));
        txtName.setText(value(rs, "farmer_name"));
        txtFather.setText(value(rs, "father_name"));
        setCombo(cmbGender, value(rs, "gender"));
        txtDob.setText(value(rs, "dob"));
        txtMobile.setText(value(rs, "mobile"));
        txtAltMobile.setText(value(rs, "alternate_mobile"));
        txtVillage.setText(value(rs, "village"));
        txtPost.setText(value(rs, "post"));
        txtBlock.setText(value(rs, "block"));
        txtDistrict.setText(value(rs, "district"));
        txtState.setText(value(rs, "state"));
        txtPin.setText(value(rs, "pin_code"));
        txtBankName.setText(value(rs, "bank_name"));
        txtBranch.setText(value(rs, "branch"));
        txtIfsc.setText(value(rs, "ifsc"));
        txtAccountNo.setText(firstValue(rs, "account_no", "bank_account"));
        txtAccountHolder.setText(value(rs, "account_holder"));
        txtAadhaar.setText(value(rs, "aadhaar"));
        txtPan.setText(value(rs, "pan"));
        setCombo(cmbMilkType, value(rs, "milk_type"));
        txtCenterName.setText(value(rs, "center_name"));
        txtJoiningDate.setText(value(rs, "joining_date"));
        setCombo(cmbStatus, value(rs, "status"));

        selectedPhotoPath = value(rs, "photo_path");
        showPhoto(selectedPhotoPath);
    }

    private String firstValue(ResultSet rs, String first, String second) throws Exception {
        String value = value(rs, first);
        return value.isEmpty() ? value(rs, second) : value;
    }

    private String value(ResultSet rs, String column) throws Exception {
        if (!farmerColumns.contains(column)) {
            return "";
        }
        Object value = rs.getObject(column);
        return value == null ? "" : String.valueOf(value);
    }

    private String selectColumn(String column) {
        return farmerColumns.contains(column) ? ", " + column : ", NULL AS " + column;
    }

    private void loadFarmerColumns() {
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                loadFarmerColumns(con);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadFarmerColumns(Connection con) throws Exception {
        farmerColumns.clear();
        DatabaseMetaData meta = con.getMetaData();
        try (ResultSet rs = meta.getColumns(con.getCatalog(), null, "farmers", null)) {
            while (rs.next()) {
                farmerColumns.add(rs.getString("COLUMN_NAME"));
            }
        }

        if (farmerColumns.isEmpty()) {
            try (PreparedStatement ps = con.prepareStatement("SHOW COLUMNS FROM farmers");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    farmerColumns.add(rs.getString("Field"));
                }
            }
        }
    }

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            selectedPhotoPath = file.getAbsolutePath();
            showPhoto(selectedPhotoPath);
        }
    }

    private void showPhoto(String path) {
        if (path == null || path.trim().isEmpty() || !new File(path).exists()) {
            lblPhoto.setIcon(null);
            lblPhoto.setText("No Photo");
            return;
        }

        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(125, 145, Image.SCALE_SMOOTH);
        lblPhoto.setText("");
        lblPhoto.setIcon(new ImageIcon(img));
    }

    private JPanel cardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 224, 232)),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(4, 8, 8, 8),
                        title,
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        new Color(45, 55, 72)
                )
        ));
        return panel;
    }

    private JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(inputFont);
        field.setPreferredSize(new Dimension(165, 28));
        return field;
    }

    private JComboBox<String> combo(String... values) {
        JComboBox<String> box = new JComboBox<>(values);
        box.setFont(inputFont);
        box.setPreferredSize(new Dimension(165, 28));
        return box;
    }

    private JButton actionButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(112, 36));
        return button;
    }

    private void addField(JPanel panel, int row, String label, JComponent input) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 3, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(labelFont);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(input, gbc);
    }

    private String selected(JComboBox<String> combo) {
        Object item = combo.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private void setCombo(JComboBox<String> combo, String value) {
        if (value == null || value.trim().isEmpty()) {
            combo.setSelectedIndex(0);
            return;
        }

        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equalsIgnoreCase(value.trim())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FarmerManagement::new);
    }
}
