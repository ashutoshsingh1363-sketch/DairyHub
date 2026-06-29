package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.time.LocalDate;
import database.DBConnection;

public class ReceiptFrame extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JScrollPane tableScrollPane;
    private JLabel lblReceiptNo;
    private JLabel lblPaymentDate;
    private JLabel lblTotalMilk;
    private JLabel lblTotalAmount;
    private JLabel lblAmountWords;
    private JTextArea txtRemarks;
    private JButton btnBack;
    private JButton btnPrint;
    private JPanel receiptPanel;

    public ReceiptFrame(
            String farmerId,
            String farmerName,
            String fromDate,
            String toDate,
            String totalMilk,
            String totalAmount,
            String remarks) {

        setTitle("DAIRY HUB - Payment Receipt");
        setSize(1000, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        receiptPanel = new JPanel(new BorderLayout());
        receiptPanel.setBackground(Color.WHITE);
        receiptPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        receiptPanel.add(createReceiptBody(farmerId, farmerName, fromDate, toDate, totalMilk, totalAmount, remarks), BorderLayout.CENTER);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        container.add(receiptPanel, BorderLayout.CENTER);

        add(container, BorderLayout.CENTER);
        add(createFooterButtons(), BorderLayout.SOUTH);

        loadCollectionData(farmerId, fromDate, toDate);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(null);
        header.setPreferredSize(new Dimension(1000, 140));
        header.setBackground(new Color(11, 60, 118));

        JLabel logo = createLogoLabel();
        logo.setBounds(20, 18, 145, 90);
        header.add(logo);

        JLabel title = new JLabel("DAIRY HUB");
        title.setFont(new Font("Segoe UI", Font.BOLD, 44));
        title.setForeground(Color.WHITE);
        title.setBounds(190, 10, 420, 50);
        header.add(title);

        JLabel subtitle = new JLabel("MILK COLLECTION & PAYMENT SYSTEM");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(238, 242, 246));
        subtitle.setBounds(190, 55, 470, 30);
        header.add(subtitle);

        JLabel receiptTitle = new JLabel("PAYMENT RECEIPT");
        receiptTitle.setHorizontalAlignment(SwingConstants.CENTER);
        receiptTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        receiptTitle.setForeground(Color.WHITE);
        receiptTitle.setBackground(new Color(3, 69, 156));
        receiptTitle.setOpaque(true);
        receiptTitle.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        receiptTitle.setBounds(640, 40, 260, 40);
        header.add(receiptTitle);

        JLabel address = new JLabel("Near Bus Stand, Bhakhua, Kaimur (Bihar) - 821101");
        address.setForeground(Color.WHITE);
        address.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        address.setBounds(190, 90, 420, 18);
        header.add(address);

        JLabel contact = new JLabel("+91 9123456789 | dairyhub@gmail.com");
        contact.setForeground(new Color(224, 231, 244));
        contact.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contact.setBounds(190, 110, 420, 18);
        header.add(contact);

        lblReceiptNo = createHeaderLabel("Receipt Number : DH" + System.currentTimeMillis() % 100000, 640, 95, 260, 20);
        header.add(lblReceiptNo);

        lblPaymentDate = createHeaderLabel("Payment Date & Time : " + LocalDate.now().toString(), 640, 120, 260, 20);
        header.add(lblPaymentDate);

        return header;
    }

    private JLabel createHeaderLabel(String text, int x, int y, int w, int h) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setBounds(x, y, w, h);
        return label;
    }

    private JLabel createLogoLabel() {
        String logoPath = "C:\\Users\\sadhu\\Downloads\\dairy hub logo.png";
        JLabel logo = new JLabel("DAIRY HUB");
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setForeground(new Color(11, 60, 118));
        logo.setOpaque(true);
        logo.setBackground(Color.WHITE);
        logo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(11, 60, 118), 2),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        File logoFile = new File(logoPath);
        if (logoFile.exists()) {
            ImageIcon icon = new ImageIcon(logoPath);
            Image img = icon.getImage().getScaledInstance(145, 90, Image.SCALE_SMOOTH);
            logo.setText("");
            logo.setIcon(new ImageIcon(img));
        }
        return logo;
    }

    private JPanel createReceiptBody(String farmerId, String farmerName, String fromDate, String toDate, String totalMilk, String totalAmount, String remarks) {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        body.setBackground(Color.WHITE);

        JPanel detailsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        detailsPanel.setOpaque(false);
        detailsPanel.add(createInfoCard("FARMER DETAILS", new String[][]{
                {"Farmer ID", farmerId},
                {"Farmer Name", farmerName},
                {"Address", "Kripalpur, Durgawati Prkhand, Kaimur (Bihar) - 821105"},
                {"Phone", "+91 9123456789"}
        }));
        detailsPanel.add(createInfoCard("PAYMENT PERIOD", new String[][]{
                {"From Date", fromDate},
                {"To Date", toDate},
                {"Total Days", calculateDays(fromDate, toDate)}
        }));
        detailsPanel.add(createInfoCard("OPERATOR DETAILS", new String[][]{
                {"Operator ID", "OP101"},
                {"Operator Name", "Admin (Ashutosh)"}
        }));

        body.add(detailsPanel, BorderLayout.NORTH);
        body.add(createCollectionTableSection(), BorderLayout.CENTER);
        body.add(createSummarySection(totalMilk, totalAmount, remarks), BorderLayout.SOUTH);

        return body;
    }

    private JPanel createInfoCard(String title, String[][] rows) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(247, 249, 253));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(14, 59, 114), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        card.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(11, 60, 118));
        gbc.weightx = 1.0;
        card.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 4, 0);
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(1, 1));
        card.add(separator, gbc);

        gbc.insets = new Insets(6, 0, 2, 0);
        for (String[] row : rows) {
            gbc.gridy++;
            JPanel rowPanel = new JPanel(new BorderLayout(8, 0));
            rowPanel.setOpaque(false);
            JLabel label = new JLabel(row[0] + " :");
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(new Color(15, 55, 115));
            JLabel value = new JLabel(row[1]);
            value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            value.setForeground(new Color(45, 55, 72));
            rowPanel.add(label, BorderLayout.WEST);
            rowPanel.add(value, BorderLayout.CENTER);
            card.add(rowPanel, gbc);
        }
        return card;
    }

    private String calculateDays(String from, String to) {
        try {
            LocalDate start = LocalDate.parse(from);
            LocalDate end = LocalDate.parse(to);
            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            return days + " Days";
        } catch (Exception ex) {
            return "-";
        }
    }

    private JScrollPane createCollectionTableSection() {
        model = new DefaultTableModel();
        model.addColumn("S.No.");
        model.addColumn("Date");
        model.addColumn("Shift");
        model.addColumn("Milk Type");
        model.addColumn("Milk (Ltr)");
        model.addColumn("FAT (%)");
        model.addColumn("Rate / Ltr (Rs.)");
        model.addColumn("Amount (Rs.)");

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(230, 236, 250));
        table.getTableHeader().setForeground(new Color(19, 48, 90));
        table.setGridColor(new Color(216, 226, 243));
        table.setFillsViewportHeight(true);

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(14, 59, 114), 1), "DAILY MILK COLLECTION DETAILS", TitledBorder.CENTER, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12), new Color(11, 60, 118)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        return tableScrollPane;
    }

    private JPanel createSummarySection(String totalMilk, String totalAmount, String remarks) {
        JPanel summary = new JPanel(new GridLayout(1, 3, 16, 0));
        summary.setOpaque(false);
        summary.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        summary.add(createValueCard("GRAND TOTAL MILK", totalMilk + " Ltr", null));
        summary.add(createValueCard("GRAND TOTAL AMOUNT", "Rs. " + totalAmount, "Rs."));
        summary.add(createRemarkCard("AMOUNT IN WORDS", convertAmountToWords(totalAmount)));

        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setOpaque(false);
        container.add(summary, BorderLayout.CENTER);

        txtRemarks = new JTextArea(remarks);
        txtRemarks.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        txtRemarks.setEditable(false);
        txtRemarks.setBackground(new Color(248, 249, 251));
        txtRemarks.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 214, 232)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel remarksPanel = new JPanel(new BorderLayout());
        remarksPanel.setOpaque(false);
        remarksPanel.add(new JLabel("REMARKS"), BorderLayout.NORTH);
        remarksPanel.add(txtRemarks, BorderLayout.CENTER);
        remarksPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        container.add(remarksPanel, BorderLayout.SOUTH);
        return container;
    }

    private JPanel createValueCard(String title, String value, String iconText) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(250, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(204, 217, 238), 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(17, 60, 112));
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(14, 59, 114));
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRemarkCard(String title, String text) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(true);
        card.setBackground(new Color(248, 249, 251));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(4, 105, 72), 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(4, 105, 72));
        card.add(titleLabel, BorderLayout.NORTH);

        lblAmountWords = new JLabel(" ");
        lblAmountWords.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAmountWords.setForeground(new Color(36, 50, 73));
        lblAmountWords.setVerticalAlignment(SwingConstants.TOP);
        lblAmountWords.setText(String.format("<html><body style='width:220px;'>%s</body></html>", text));
        card.add(lblAmountWords, BorderLayout.CENTER);

        return card;
    }

    private JPanel createFooterButtons() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(Color.WHITE);

        btnPrint = new JButton("Print Receipt");
        btnPrint.setBackground(Color.WHITE);
        btnPrint.setForeground(Color.BLACK);
        btnPrint.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(11, 60, 118), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrint.setFocusPainted(false);
        btnPrint.addActionListener(e -> printReceipt());

        btnBack = new JButton("Back");
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(Color.BLACK);
        btnBack.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(125, 134, 162), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> dispose());

        footer.add(btnPrint);
        footer.add(btnBack);
        return footer;
    }

    private void loadCollectionData(String farmerId, String fromDate, String toDate) {
        if (farmerId == null || fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Missing query parameters.");
            return;
        }
        farmerId = farmerId.trim();
        fromDate = fromDate.trim();
        toDate = toDate.trim();

        int idVal;
        try {
            idVal = Integer.parseInt(farmerId);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Farmer ID: " + farmerId);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed in Receipt view.");
                return;
            }
            String sql = "SELECT collection_date, shift, milk_type, quantity, fat, snf, rate, total_amount " +
                    "FROM milk_collection WHERE farmer_id=? AND collection_date BETWEEN ? AND ? ORDER BY collection_date";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idVal);
                ps.setDate(2, java.sql.Date.valueOf(fromDate));
                ps.setDate(3, java.sql.Date.valueOf(toDate));
                try (ResultSet rs = ps.executeQuery()) {
                    int index = 1;
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                index++,
                                rs.getDate("collection_date"),
                                rs.getString("shift"),
                                rs.getString("milk_type"),
                                rs.getDouble("quantity"),
                                rs.getDouble("fat"),
                                rs.getDouble("rate"),
                                rs.getDouble("total_amount")
                        });
                    }
                }
            }
            if (table != null) {
                table.revalidate();
                int headerHeight = (table.getTableHeader() != null) ? table.getTableHeader().getPreferredSize().height : 28;
                if (headerHeight <= 0) headerHeight = 28;
                int tableHeight = table.getRowHeight() * table.getRowCount() + headerHeight;
                int tableWidth = Math.max(800, table.getPreferredSize().width);
                Dimension size = new Dimension(tableWidth, tableHeight);
                table.setPreferredScrollableViewportSize(size);
                if (tableScrollPane != null) {
                    tableScrollPane.setPreferredSize(size);
                    tableScrollPane.revalidate();
                }
            }
            if (receiptPanel != null) {
                receiptPanel.revalidate();
                receiptPanel.repaint();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading receipt details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Dairy Hub Payment Receipt");
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Dimension size = receiptPanel.getPreferredSize();
            double scale = pageFormat.getImageableWidth() / size.width;
            double pageHeight = pageFormat.getImageableHeight() / scale;
            int totalPages = (int) Math.ceil(size.height / pageHeight);

            if (pageIndex >= totalPages) {
                return Printable.NO_SUCH_PAGE;
            }

            g2.scale(scale, scale);
            g2.translate(0, -pageIndex * pageHeight);
            receiptPanel.printAll(g2);
            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage());
            }
        }
    }

    private String convertAmountToWords(String amountString) {
        try {
            double amount = Double.parseDouble(amountString);
            long rupees = (long) amount;
            int paise = (int) Math.round((amount - rupees) * 100);
            String rupeeWords = numberToWords(rupees) + " Rupees";
            if (paise > 0) {
                rupeeWords += " and " + numberToWords(paise) + " Paise";
            }
            return rupeeWords + " Only";
        } catch (Exception ex) {
            return "";
        }
    }

    private String numberToWords(long number) {
        if (number == 0) {
            return "Zero";
        }
        if (number < 0) {
            return "Minus " + numberToWords(Math.abs(number));
        }

        String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

        String words = "";

        if (number / 10000000 > 0) {
            words += numberToWords(number / 10000000) + " Crore ";
            number %= 10000000;
        }
        if (number / 100000 > 0) {
            words += numberToWords(number / 100000) + " Lakh ";
            number %= 100000;
        }
        if (number / 1000 > 0) {
            words += numberToWords(number / 1000) + " Thousand ";
            number %= 1000;
        }
        if (number / 100 > 0) {
            words += numberToWords(number / 100) + " Hundred ";
            number %= 100;
        }
        if (number > 0) {
            if (number < 20) {
                words += units[(int) number];
            } else {
                words += tens[(int) (number / 10)];
                if (number % 10 > 0) {
                    words += " " + units[(int) (number % 10)];
                }
            }
        }
        return words.trim();
    }
}
