package ui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

public class OperatorFrame extends JFrame {

    private JPanel mainPanel;
    private final DefaultTableModel tableModel;
    private JTextField txtOperatorId;
    private JTextField txtName;
    private JTextField txtMobile;
    private JTextField txtRole;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbStatus;
    private final boolean embedded;

    public OperatorFrame() {
        this(false);
    }

    public OperatorFrame(boolean embedded) {
        this.embedded = embedded;
        setTitle("DAIRY HUB - Operators");
        setSize(980, 620);
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

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(18, 76, 140));
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel title = new JLabel("Operators");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 232)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtOperatorId = createTextField("OP" + System.currentTimeMillis() % 100000);
        txtName = createTextField("");
        txtMobile = createTextField("");
        txtRole = createTextField("Operator");
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(190, 34));
        cmbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        addField(formPanel, gbc, 0, "Operator ID", txtOperatorId);
        addField(formPanel, gbc, 1, "Operator Name", txtName);
        addField(formPanel, gbc, 2, "Mobile", txtMobile);
        addField(formPanel, gbc, 3, "Role", txtRole);
        addField(formPanel, gbc, 4, "Password", txtPassword);
        addField(formPanel, gbc, 5, "Status", cmbStatus);

        JButton btnSave = createButton("Save Operator", new Color(39, 174, 96));
        JButton btnClear = createButton("Clear", new Color(149, 165, 166));
        JButton btnBack = createButton("Back", new Color(192, 57, 43));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(btnSave);
        buttons.add(btnClear);
        buttons.add(btnBack);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        formPanel.add(buttons, gbc);

        mainPanel.add(formPanel, BorderLayout.WEST);

        String[] columns = {"Operator ID", "Name", "Mobile", "Role", "Status", "Joining Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        btnSave.addActionListener(e -> saveOperator());
        btnClear.addActionListener(e -> clearForm());
        btnBack.addActionListener(e -> closeCurrentTab());

        loadOperators();
        if (!embedded) {
            setVisible(true);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private JTextField createTextField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(190, 34));
        return field;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(background);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(140, 38));
        return button;
    }

    private void saveOperator() {
        String operatorId = txtOperatorId.getText().trim();
        String name = txtName.getText().trim();
        String mobile = txtMobile.getText().trim();
        String role = txtRole.getText().trim();
        String password = String.valueOf(txtPassword.getPassword()).trim();
        String status = cmbStatus.getSelectedItem().toString();

        if (operatorId.isEmpty() || name.isEmpty() || mobile.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter operator ID, name, mobile and password.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }
            ensureTableExists(con);

            String sql = "INSERT INTO operators (operator_id, operator_name, mobile, role_name, login_password, status, joining_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                    "operator_name=VALUES(operator_name), mobile=VALUES(mobile), role_name=VALUES(role_name), " +
                    "login_password=VALUES(login_password), status=VALUES(status)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, operatorId);
                ps.setString(2, name);
                ps.setString(3, mobile);
                ps.setString(4, role);
                ps.setString(5, password);
                ps.setString(6, status);
                ps.setDate(7, java.sql.Date.valueOf(LocalDate.now()));
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Operator saved successfully.");
            clearForm();
            loadOperators();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to save operator: " + ex.getMessage());
        }
    }

    private void clearForm() {
        txtOperatorId.setText("OP" + System.currentTimeMillis() % 100000);
        txtName.setText("");
        txtMobile.setText("");
        txtRole.setText("Operator");
        txtPassword.setText("");
        cmbStatus.setSelectedIndex(0);
        txtName.requestFocus();
    }

    private void loadOperators() {
        tableModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return;
            }
            ensureTableExists(con);
            String sql = "SELECT operator_id, operator_name, mobile, role_name, status, joining_date FROM operators ORDER BY operator_name";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getString("operator_id"),
                            rs.getString("operator_name"),
                            rs.getString("mobile"),
                            rs.getString("role_name"),
                            rs.getString("status"),
                            rs.getDate("joining_date")
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void ensureTableExists(Connection con) throws Exception {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS operators (" +
                    "operator_id VARCHAR(30) PRIMARY KEY, " +
                    "operator_name VARCHAR(100) NOT NULL, " +
                    "mobile VARCHAR(15) NOT NULL, " +
                    "role_name VARCHAR(50), " +
                    "login_password VARCHAR(50), " +
                    "status VARCHAR(20), " +
                    "joining_date DATE NOT NULL)");
        }
        try (Statement st = con.createStatement()) {
            st.executeUpdate("ALTER TABLE operators ADD COLUMN login_password VARCHAR(50)");
        } catch (Exception ignored) {
            // Column already exists in upgraded databases.
        }
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
}
