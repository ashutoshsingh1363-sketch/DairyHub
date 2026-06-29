package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDate;
import database.DBConnection;

public class ReportFrame extends JFrame {

    private JPanel mainPanel;
    private final boolean embedded;
    // UI Components
    private JTextField txtFromDate, txtToDate;
    private JComboBox<String> cmbShift, cmbMilkType;
    private JButton btnSearch, btnClear;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    
    // Summary Cards Labels
    private JLabel lblTotalLitersVal, lblAvgFatVal, lblAvgClrVal, lblTotalAmtVal;
    
    // Footer Action Buttons
    private JButton btnPrint, btnPdf, btnExcel, btnClose;

    // Formatter for Currency and Decimals
    private final DecimalFormat dfQty = new DecimalFormat("0.00");
    private final DecimalFormat dfAmt = new DecimalFormat("'Rs.' 0.00");
    private final DecimalFormat dfRate = new DecimalFormat("0.00");

    public ReportFrame() {
        this(false);
    }

    public ReportFrame(boolean embedded) {
        this.embedded = embedded;
        // Window Configuration
        setTitle("Commercial Dairy Software - Advanced Milk Report Module");
        setSize(1100, 750);
        if (!embedded) {
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(950, 600));

        // Main Panel with Border Layout
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 247, 250)); 
        if (!embedded) {
            setContentPane(mainPanel);
        }

        // Sub-panels Initialization
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        JPanel centerBodyPanel = new JPanel(new BorderLayout(10, 15));
        centerBodyPanel.setOpaque(false);
        centerBodyPanel.add(createFilterPanel(), BorderLayout.NORTH);
        
        JPanel contentGrid = new JPanel(new BorderLayout(10, 10));
        contentGrid.setOpaque(false);
        contentGrid.add(createSummaryPanel(), BorderLayout.NORTH);
        contentGrid.add(createTablePanel(), BorderLayout.CENTER);
        
        centerBodyPanel.add(contentGrid, BorderLayout.CENTER);

        JScrollPane centerScroll = new JScrollPane(centerBodyPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        centerScroll.setBorder(BorderFactory.createEmptyBorder());
        centerScroll.getVerticalScrollBar().setUnitIncrement(14);

        mainPanel.add(centerScroll, BorderLayout.CENTER);
        
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        // Wire up Event Handlers & Initial Data Load
        initEventHandlers();
        SwingUtilities.invokeLater(this::loadReportData);
        if (!embedded) {
            setVisible(true);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(26, 54, 93)); 
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("MILK COLLECTION & SUMMARY REPORT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Commercial Dairy Management System v1.0");
        lblSubtitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSubtitle.setForeground(new Color(226, 232, 240));

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblSubtitle, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 224), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);

        // Row 1: Dates
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("From Date (YYYY-MM-DD):"), gbc);
        
        gbc.gridx = 1;
        txtFromDate = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
        txtFromDate.setFont(inputFont);
        filterPanel.add(txtFromDate, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("To Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 3;
        txtToDate = new JTextField(LocalDate.now().toString(), 10);
        txtToDate.setFont(inputFont);
        filterPanel.add(txtToDate, gbc);

        // Row 2: Shift & Milk Type
        gbc.gridx = 0; gbc.gridy = 1;
        filterPanel.add(new JLabel("Collection Shift:"), gbc);

        gbc.gridx = 1;
        cmbShift = new JComboBox<>(new String[]{"All Shifts", "Morning", "Evening"});
        cmbShift.setFont(inputFont);
        filterPanel.add(cmbShift, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("Milk Type:"), gbc);

        gbc.gridx = 3;
        cmbMilkType = new JComboBox<>(new String[]{"All Types", "Cow", "Buffalo"});
        cmbMilkType.setFont(inputFont);
        filterPanel.add(cmbMilkType, gbc);

        // Buttons
        gbc.gridx = 4; gbc.gridy = 0;
        btnSearch = new JButton("Search Report");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.setBackground(new Color(49, 151, 149)); 
        btnSearch.setForeground(Color.BLACK);
        btnSearch.setFocusPainted(false);
        filterPanel.add(btnSearch, gbc);

        gbc.gridx = 4; gbc.gridy = 1;
        btnClear = new JButton("Reset Filters");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClear.setBackground(new Color(226, 232, 240));
        btnClear.setForeground(Color.BLACK);
        btnClear.setFocusPainted(false);
        filterPanel.add(btnClear, gbc);

        return filterPanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setOpaque(false);

        summaryPanel.add(createCard("TOTAL QUANTITY", lblTotalLitersVal = new JLabel("0.00 Ltr"), new Color(43, 108, 176)));
        summaryPanel.add(createCard("AVERAGE FAT", lblAvgFatVal = new JLabel("0.00 %"), new Color(221, 107, 32)));
        summaryPanel.add(createCard("AVERAGE CLR / SNF", lblAvgClrVal = new JLabel("0.00"), new Color(113, 128, 150)));
        summaryPanel.add(createCard("TOTAL NET AMOUNT", lblTotalAmtVal = new JLabel("Rs. 0.00"), new Color(56, 161, 105)));

        return summaryPanel;
    }

    private JPanel createCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                        new EmptyBorder(12, 15, 12, 15)
                )
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(113, 128, 150));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(45, 55, 72));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 224), 1),
                " Detailed Collection Log ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(45, 55, 72)
        ));

        String[] columns = {"Date", "Shift", "Member ID", "Member Name", "Milk Type", "Qty (Ltr)", "Fat (%)", "SNF", "Rate/Ltr", "Net Amount"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(26);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reportTable.setGridColor(new Color(241, 245, 249));
        reportTable.setSelectionBackground(new Color(235, 248, 250));
        reportTable.setSelectionForeground(Color.BLACK);

        JTableHeader header = reportTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(74, 85, 104));
        header.setPreferredSize(new Dimension(header.getWidth(), 30));

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        btnPrint = createFooterButton("Print Report", new Color(66, 153, 225));
        btnPdf = createFooterButton("Export PDF", new Color(229, 62, 62));
        btnExcel = createFooterButton("Export Excel", new Color(72, 187, 120));
        btnClose = createFooterButton("Back", new Color(160, 174, 192));

        footerPanel.add(btnPrint);
        footerPanel.add(btnPdf);
        footerPanel.add(btnExcel);

        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 30));
        footerPanel.add(separator);

        footerPanel.add(btnClose);

        return footerPanel;
    }

    private JButton createFooterButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(background);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(135, 36));
        return button;
    }

    /**
     * DATABASE LOGIC & EVENT CONTROLLERS (PART-2 FEATURE)
     */
    private void initEventHandlers() {
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadReportData();
            }
        });

        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtFromDate.setText(LocalDate.now().withDayOfMonth(1).toString());
                txtToDate.setText(LocalDate.now().toString());
                cmbShift.setSelectedIndex(0);
                cmbMilkType.setSelectedIndex(0);
                loadReportData();
            }
        });

        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (embedded) {
                    Container parent = mainPanel.getParent();
                    while (parent != null && !(parent instanceof JTabbedPane)) {
                        parent = parent.getParent();
                    }
                    if (parent instanceof JTabbedPane) {
                        JTabbedPane tabs = (JTabbedPane) parent;
                        tabs.remove(mainPanel);
                    }
                } else {
                    dispose();
                }
            }
        });

        btnPrint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    MessageFormat header = new MessageFormat("DAIRY HUB - Milk Collection Report");
                    MessageFormat footer = new MessageFormat("Page {0}");
                    boolean complete = reportTable.print(JTable.PrintMode.FIT_WIDTH, header, footer, true, null, true, null);
                    if (complete) {
                        JOptionPane.showMessageDialog(ReportFrame.this,
                                "Report Printed Successfully", "Print Complete", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ReportFrame.this,
                                "Printing was canceled.", "Print Canceled", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(ReportFrame.this,
                            "Unable to print report: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        btnPdf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "PDF Export Coming Soon");
            }
        });

        btnExcel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "Excel Export Coming Soon");
            }
        });
    }

    /**
     * Simulates Database Fetching via SQL Engine Architecture
     * (Fully operational and structured to switch to SQL JDBC instantly)
     */
    private void loadReportData() {
        tableModel.setRowCount(0);

        String fromDateStr = txtFromDate.getText().trim();
        String toDateStr = txtToDate.getText().trim();
        String selectedShift = cmbShift.getSelectedItem().toString();
        String selectedMilkType = cmbMilkType.getSelectedItem().toString();

        java.sql.Date fromDate;
        java.sql.Date toDate;
        try {
            fromDate = java.sql.Date.valueOf(fromDateStr);
            toDate = java.sql.Date.valueOf(toDateStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid dates in YYYY-MM-DD format.");
            return;
        }

        double totalLiters = 0;
        double totalFatSum = 0;
        double totalSnfSum = 0;
        double totalAmount = 0;
        int matchingRecordCount = 0;

        String sql = "SELECT m.collection_date, m.shift, m.farmer_id, f.farmer_name, " +
                "m.milk_type, m.quantity, m.fat, m.snf, m.rate, m.total_amount " +
                "FROM milk_collection m " +
                "LEFT JOIN farmers f ON m.farmer_id = f.farmer_id " +
                "WHERE m.collection_date BETWEEN ? AND ?";

        if (!selectedShift.equals("All Shifts")) {
            sql += " AND m.shift = ?";
        }
        if (!selectedMilkType.equals("All Types")) {
            sql += " AND m.milk_type = ?";
        }
        sql += " ORDER BY m.collection_date";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDate(1, fromDate);
                ps.setDate(2, toDate);
                int index = 3;
                if (!selectedShift.equals("All Shifts")) {
                    ps.setString(index++, selectedShift);
                }
                if (!selectedMilkType.equals("All Types")) {
                    ps.setString(index, selectedMilkType);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double quantity = rs.getDouble("quantity");
                        double fat = rs.getDouble("fat");
                        double snf = rs.getDouble("snf");
                        double rate = rs.getDouble("rate");
                        double amount = rs.getDouble("total_amount");

                        tableModel.addRow(new Object[]{
                                rs.getDate("collection_date"),
                                rs.getString("shift"),
                                rs.getString("farmer_id"),
                                rs.getString("farmer_name"),
                                rs.getString("milk_type"),
                                dfQty.format(quantity),
                                dfQty.format(fat),
                                dfQty.format(snf),
                                dfRate.format(rate),
                                dfAmt.format(amount)
                        });

                        totalLiters += quantity;
                        totalFatSum += fat;
                        totalSnfSum += snf;
                        totalAmount += amount;
                        matchingRecordCount++;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to load report data: " + ex.getMessage());
            return;
        }

        double avgFat = (matchingRecordCount > 0) ? (totalFatSum / matchingRecordCount) : 0.0;
        double avgSnf = (matchingRecordCount > 0) ? (totalSnfSum / matchingRecordCount) : 0.0;

        lblTotalLitersVal.setText(dfQty.format(totalLiters) + " Ltr");
        lblAvgFatVal.setText(dfQty.format(avgFat) + " %");
        lblAvgClrVal.setText(dfQty.format(avgSnf));
        lblTotalAmtVal.setText(dfAmt.format(totalAmount));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ReportFrame frame = new ReportFrame();
            frame.setVisible(true);
        });
    }
}