package ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import dao.MilkDAO;
import service.DatabaseBackupService;

public class Dashboard extends JFrame {

    private static final String PROFILE_PHOTO_PATH_FILE = "profile_photo_path.txt";

    private JLabel lblTodayMilk;
    private JLabel lblTodayAmount;
    private JTabbedPane contentTabs;
    private ImageIcon profilePhotoIcon;
    private String savedPhotoPath = "";
    private String currentUserRole = "Admin";
    private String currentDisplayName = "Admin";

    public Dashboard() {
        this("Admin", "Admin");
    }

    public Dashboard(String userRole, String displayName) {
        currentUserRole = userRole == null ? "Admin" : userRole;
        currentDisplayName = displayName == null || displayName.trim().isEmpty() ? currentUserRole : displayName;
        boolean isAdmin = "Admin".equalsIgnoreCase(currentUserRole);
        setTitle("DAIRY HUB - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(new Color(18, 76, 140));
        header.setPreferredSize(new Dimension(0, 110));
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("DAIRY HUB");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JLabel sub = new JLabel("Milk Collection Management System");
        sub.setForeground(new Color(206, 229, 255));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        String welcomeMsg;
        int currentHour = java.time.LocalTime.now().getHour();
        if (currentHour < 12) {
            welcomeMsg = "Good Morning, " + currentDisplayName;
        } else if (currentHour < 17) {
            welcomeMsg = "Good Afternoon, " + currentDisplayName;
        } else {
            welcomeMsg = "Good Evening, " + currentDisplayName;
        }
        JLabel welcomeLabel = new JLabel(welcomeMsg);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel dateTimeLabel = new JLabel();
        dateTimeLabel.setForeground(new Color(230, 240, 255));
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Start a dynamic swing timer to update system date-time
        javax.swing.Timer dateTimeTimer = new javax.swing.Timer(1000, e -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy | Current Time: hh:mm:ss a");
            dateTimeLabel.setText(now.format(dtf));
        });
        dateTimeTimer.start();

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(sub);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(welcomeLabel);
        titlePanel.add(dateTimeLabel);
        header.add(titlePanel, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightActions.setOpaque(false);

        JButton menuToggle = new JButton("Menu");
        menuToggle.setFocusPainted(false);
        menuToggle.setBackground(new Color(255, 255, 255, 220));
        menuToggle.setForeground(new Color(18, 76, 140));
        menuToggle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menuToggle.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JButton notificationBtn = new JButton("Notifications");
        notificationBtn.setFocusPainted(false);
        notificationBtn.setBackground(new Color(255, 255, 255, 220));
        notificationBtn.setForeground(new Color(18, 76, 140));
        notificationBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        notificationBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        notificationBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "No new notifications."));

        JButton profileBtn = new JButton("Admin Profile");
        profileBtn.setFocusPainted(false);
        profileBtn.setBackground(new Color(255, 255, 255, 220));
        profileBtn.setForeground(new Color(18, 76, 140));
        profileBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        profileBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        profileBtn.addActionListener(e -> openTab("Admin Profile", createProfilePanelClean()));

        JButton headerSettingsBtn = new JButton("Settings");
        headerSettingsBtn.setFocusPainted(false);
        headerSettingsBtn.setBackground(new Color(255, 255, 255, 220));
        headerSettingsBtn.setForeground(new Color(18, 76, 140));
        headerSettingsBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headerSettingsBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        headerSettingsBtn.addActionListener(e -> {
            if (isAdmin) {
                openTab("Settings", createSettingsPanel());
            } else {
                JOptionPane.showMessageDialog(this, "Only admin can open settings.");
            }
        });

        rightActions.add(menuToggle);
        rightActions.add(notificationBtn);
        rightActions.add(profileBtn);
        rightActions.add(headerSettingsBtn);
        header.add(rightActions, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(new Color(34, 44, 62));
        sidePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(57, 71, 92), 2),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JButton dashboardBtn = createNavButton("Dashboard", new Color(52, 152, 219));
        JButton farmerBtn = createNavButton("Farmer Management", new Color(52, 152, 219));
        farmerBtn.addActionListener(e -> openTab("Farmer Management", new FarmerManagement(true).getMainPanel()));
        JButton milkBtn = createNavButton("Milk Collection", new Color(46, 204, 113));
        milkBtn.addActionListener(e -> openTab("Milk Collection", new MilkCollection(true).getMainPanel()));
        JButton rateChartBtn = createNavButton("Rate Chart", new Color(142, 68, 173));
        rateChartBtn.addActionListener(e -> openTab("Rate Chart", new RateChartFrame(true).getMainPanel()));
        JButton paymentBtn = createNavButton("Payments", new Color(241, 196, 15));
        paymentBtn.addActionListener(e -> openTab("Payments", new PaymentFrame(true).getMainPanel()));
        JButton feedBtn = createNavButton("Feed Management", new Color(230, 126, 34));
        feedBtn.addActionListener(e -> openTab("Feed Management", new FeedManagementFrame(true).getMainPanel()));
        JButton billsBtn = createNavButton("Bills", new Color(155, 89, 182));
        billsBtn.addActionListener(e -> openTab("Bills", new BillFrame(true).getMainPanel()));
        JButton reportBtn = createNavButton("Reports", new Color(231, 76, 60));
        reportBtn.addActionListener(e -> {
            try {
                openTab("Reports", new ReportFrame(true).getMainPanel());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Unable to open reports: " + ex.getMessage());
            }
        });
        JButton operatorsBtn = createNavButton("Operators", new Color(26, 188, 156));
        operatorsBtn.addActionListener(e -> openTab("Operators", new OperatorFrame(true).getMainPanel()));
        JButton notificationsBtn = createNavButton("Notifications", new Color(243, 156, 18));
        notificationsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "No new notifications."));
        JButton settingsBtn = createNavButton("Settings", new Color(149, 165, 166));
        settingsBtn.addActionListener(e -> openTab("Settings", createSettingsPanel()));
        JButton logoutBtn = createNavButton("Logout", new Color(192, 57, 43));

        sidePanel.add(dashboardBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(farmerBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(milkBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        if (isAdmin) {
            sidePanel.add(rateChartBtn);
        }
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        if (isAdmin) {
            sidePanel.add(paymentBtn);
        }
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        if (isAdmin) {
            sidePanel.add(feedBtn);
        }
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        if (isAdmin) {
            sidePanel.add(billsBtn);
        }
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(reportBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        if (isAdmin) {
            sidePanel.add(operatorsBtn);
        }
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(notificationsBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        if (isAdmin) {
            sidePanel.add(settingsBtn);
        }
        sidePanel.add(Box.createVerticalGlue());
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        sidePanel.add(logoutBtn);

        JScrollPane sideScroll = new JScrollPane(sidePanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sideScroll.setBorder(null);
        sideScroll.getVerticalScrollBar().setUnitIncrement(16);
        sideScroll.setPreferredSize(new Dimension(260, 0));
        sideScroll.getViewport().setOpaque(false);
        sideScroll.setOpaque(false);
        sideScroll.setVisible(false);
        menuToggle.addActionListener(e -> {
            sideScroll.setVisible(!sideScroll.isVisible());
            menuToggle.setText(sideScroll.isVisible() ? "Close" : "Menu");
            root.revalidate();
            root.repaint();
        });
        root.add(sideScroll, BorderLayout.WEST);

        contentTabs = new JTabbedPane();
        contentTabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        contentTabs.addTab("Home", createHomePanel());
        contentTabs.setTabComponentAt(0, createTabHeader("Home", null, false));
        root.add(contentTabs, BorderLayout.CENTER);

        loadSavedProfilePhoto();
        loadDashboardData();
        setVisible(true);
    }

    private JButton createNavButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(background);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 2),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(240, 52));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        button.setMinimumSize(new Dimension(240, 52));
        button.setBorderPainted(true);
        return button;
    }

    private JPanel createHomePanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(16, 16));
        contentPanel.setOpaque(false);

        JPanel metricsPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        metricsPanel.setOpaque(false);
        metricsPanel.add(createMetricCard("Today's Milk", lblTodayMilk = new JLabel("0.00 Ltr"), new Color(52, 152, 219)));
        metricsPanel.add(createMetricCard("Today's Amount", lblTodayAmount = new JLabel("Rs. 0.00"), new Color(46, 204, 113)));
        contentPanel.add(metricsPanel, BorderLayout.NORTH);

        JPanel infoCard = new JPanel(new BorderLayout());
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 232)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        JLabel infoLabel = new JLabel("Welcome back! Use the menu to manage dairy collections, payments, farmers, and reports.");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        infoCard.add(infoLabel, BorderLayout.CENTER);

        JPanel calendarPanel = new JPanel();
        calendarPanel.setLayout(new BoxLayout(calendarPanel, BoxLayout.Y_AXIS));
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 232)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel calendarTitle = new JLabel("Calendar Widget");
        calendarTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        calendarTitle.setForeground(new Color(18, 76, 140));
        calendarPanel.add(calendarTitle);
        calendarPanel.add(Box.createVerticalStrut(10));

        JLabel eventTitle = new JLabel("Today's Events");
        eventTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        eventTitle.setForeground(new Color(34, 44, 62));
        calendarPanel.add(eventTitle);

        JLabel milkEvent = new JLabel("- Milk Collection Timing: 06:00 AM - 10:00 AM");
        milkEvent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        milkEvent.setForeground(new Color(46, 204, 113));
        calendarPanel.add(milkEvent);

        JLabel paymentEvent = new JLabel("- Payment Due Date: 28 June 2026");
        paymentEvent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        paymentEvent.setForeground(new Color(231, 76, 60));
        calendarPanel.add(paymentEvent);

        JPanel centerStack = new JPanel(new GridLayout(2, 1, 16, 16));
        centerStack.setOpaque(false);
        centerStack.add(infoCard);
        centerStack.add(calendarPanel);
        contentPanel.add(centerStack, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBackground(new Color(18, 76, 140));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel footerTitle = new JLabel("DAIRY HUB SUPPORT");
        footerTitle.setForeground(Color.WHITE);
        footerTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        footerPanel.add(footerTitle);

        JLabel footerContact = new JLabel("Contact: +91 6207159208 | Email: dairyhub13@gmail.com");
        footerContact.setForeground(new Color(230, 240, 255));
        footerContact.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footerPanel.add(footerContact);

        JLabel footerExtra = new JLabel("Address: Dairy Hub Office, Main Market | Support: 24/7 Help Desk");
        footerExtra.setForeground(new Color(230, 240, 255));
        footerExtra.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footerPanel.add(footerExtra);

        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        return contentPanel;
    }

    private void openTab(String title, JPanel panel) {
        for (int i = 0; i < contentTabs.getTabCount(); i++) {
            if (contentTabs.getTitleAt(i).equals(title)) {
                contentTabs.setSelectedIndex(i);
                return;
            }
        }
        contentTabs.addTab(title, panel);
        int index = contentTabs.getTabCount() - 1;
        contentTabs.setTabComponentAt(index, createTabHeader(title, panel, true));
        contentTabs.setSelectedIndex(index);
    }

    private JPanel createTabHeader(String title, JPanel tabPanel, boolean closable) {
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        tabHeader.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabHeader.add(titleLabel);

        if (closable) {
            JButton closeButton = new JButton("x");
            closeButton.setToolTipText("Close " + title);
            closeButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
            closeButton.setForeground(new Color(192, 57, 43));
            closeButton.setMargin(new Insets(0, 4, 0, 4));
            closeButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            closeButton.setContentAreaFilled(false);
            closeButton.setFocusPainted(false);
            closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeButton.addActionListener(e -> {
                int tabIndex = contentTabs.indexOfComponent(tabPanel);
                if (tabIndex != -1) {
                    contentTabs.removeTabAt(tabIndex);
                }
            });
            tabHeader.add(closeButton);
        }

        return tabHeader;
    }

    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new BorderLayout(16, 16));
        settingsPanel.setBackground(new Color(245, 247, 250));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(18, 76, 140));
        settingsPanel.add(title, BorderLayout.NORTH);

        JPanel settingsGrid = new JPanel(new GridLayout(2, 2, 18, 18));
        settingsGrid.setOpaque(false);
        settingsGrid.add(createSectionCard("General", new String[][]{
                {"Language", "English / Hindi"},
                {"Date Format", "YYYY-MM-DD"},
                {"Default View", "Dashboard Home"}
        }));
        settingsGrid.add(createSectionCard("Notifications", new String[][]{
                {"System Alerts", "Enabled"},
                {"Payment Reminders", "Enabled"},
                {"Collection Reminders", "Enabled"}
        }));
        settingsGrid.add(createSectionCard("Database", new String[][]{
                {"Connection", "Local MySQL"},
                {"Database", "dairyhub"},
                {"Backup", "Use Admin Profile backup section"}
        }));
        settingsGrid.add(createSectionCard("Support", new String[][]{
                {"Mobile", "+91 6207159208"},
                {"Email", "dairyhub13@gmail.com"},
                {"Status", "Ready"}
        }));

        settingsPanel.add(settingsGrid, BorderLayout.CENTER);
        return settingsPanel;
    }

    private Color getContrastColor(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance > 0.6 ? Color.DARK_GRAY : Color.WHITE;
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout(0, 20));
        profilePanel.setBackground(new Color(245, 247, 250));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel sectionTitle = new JLabel("Admin Profile");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        sectionTitle.setForeground(new Color(18, 76, 140));
        profilePanel.add(sectionTitle, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(24, 24));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 232)),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));

        JPanel topPane = new JPanel(new BorderLayout(24, 0));
        topPane.setOpaque(false);

        JLabel photoLabel = new JLabel("Photo", SwingConstants.CENTER);
        photoLabel.setOpaque(true);
        photoLabel.setBackground(new Color(232, 238, 246));
        photoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        photoLabel.setPreferredSize(new Dimension(150, 150));
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(219, 226, 232), 2));
        if (savedPhotoPath != null && !savedPhotoPath.isEmpty() && new File(savedPhotoPath).exists()) {
            ImageIcon icon = new ImageIcon(savedPhotoPath);
            Image image = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(image));
            photoLabel.setText("");
        }

        JButton uploadPhoto = createActionButton("Upload Photo", new Color(52, 152, 219));
        uploadPhoto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uploadPhoto.setMaximumSize(new Dimension(150, 34));
        uploadPhoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                savedPhotoPath = file.getAbsolutePath();
                try {
                    Files.writeString(Paths.get(PROFILE_PHOTO_PATH_FILE), savedPhotoPath, StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                ImageIcon icon = new ImageIcon(savedPhotoPath);
                Image image = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                photoLabel.setIcon(new ImageIcon(image));
                photoLabel.setText("");
            }
        });

        JPanel photoBox = new JPanel();
        photoBox.setOpaque(false);
        photoBox.setLayout(new BoxLayout(photoBox, BoxLayout.Y_AXIS));
        photoBox.add(photoLabel);
        photoBox.add(Box.createVerticalStrut(12));
        uploadPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoBox.add(uploadPhoto);
        topPane.add(photoBox, BorderLayout.WEST);

        JPanel profileInfoCard = new JPanel(new GridLayout(4, 1, 12, 12));
        profileInfoCard.setOpaque(false);
        profileInfoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Profile Information"),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        profileInfoCard.add(createFieldPanel("Name", "Ashutosh Kumar Maurya"));
        profileInfoCard.add(createFieldPanel("Role", "Super Admin"));
        profileInfoCard.add(createFieldPanel("Admin ID", "ADM001"));
        profileInfoCard.add(createFieldPanel("Status", "Active"));
        topPane.add(profileInfoCard, BorderLayout.CENTER);

        card.add(topPane, BorderLayout.NORTH);

        JPanel detailsCard = new JPanel(new BorderLayout(0, 14));
        detailsCard.setOpaque(false);
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Administrator Details"),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JPanel detailsGrid = new JPanel(new GridLayout(3, 2, 18, 12));
        detailsGrid.setOpaque(false);
        detailsGrid.add(createFieldPanel("Mobile", "6207159208"));
        detailsGrid.add(createFieldPanel("Email", "ashutoshsingh1363@gmail.com"));
        detailsGrid.add(createFieldPanel("Address", "vill- kripalpu, post- deohalia"));
        detailsGrid.add(createFieldPanel("Dairy Name", "sabarkhanth dairy"));
        detailsGrid.add(createFieldPanel("Center Name", "kripalpur"));
        detailsGrid.add(createFieldPanel("GST Registration No.", "null"));

        detailsCard.add(detailsGrid, BorderLayout.CENTER);
        card.add(detailsCard, BorderLayout.CENTER);

        JPanel bottomGrid = new JPanel(new GridLayout(2, 2, 18, 18));
        bottomGrid.setOpaque(false);
        bottomGrid.add(createSecuritySection());
        bottomGrid.add(createPreferencesSection());
        bottomGrid.add(createActivitySection());
        bottomGrid.add(createBackupSection());
        card.add(bottomGrid, BorderLayout.SOUTH);

        profilePanel.add(card, BorderLayout.CENTER);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonBar.setOpaque(false);
        JButton editButton = createActionButton("Edit Profile", new Color(52, 152, 219));
        JButton saveButton = createActionButton("Save", new Color(39, 174, 96));
        JButton logoutButton = createActionButton("Logout", new Color(192, 57, 43));
        buttonBar.add(editButton);
        buttonBar.add(saveButton);
        buttonBar.add(logoutButton);
        profilePanel.add(buttonBar, BorderLayout.SOUTH);

        editButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Edit mode enabled. Update fields and press Save."));
        saveButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Profile saved successfully."));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        return profilePanel;
    }

    private JPanel createProfilePanelClean() {
        JPanel profilePanel = new JPanel(new BorderLayout(0, 20));
        profilePanel.setBackground(new Color(245, 247, 250));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel sectionTitle = new JLabel("Admin Profile");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        sectionTitle.setForeground(new Color(18, 76, 140));
        profilePanel.add(sectionTitle, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(24, 24));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 232)),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));

        JPanel headerRow = new JPanel(new BorderLayout(24, 0));
        headerRow.setOpaque(false);

        JLabel photoLabel = new JLabel("Photo", SwingConstants.CENTER);
        photoLabel.setOpaque(true);
        photoLabel.setBackground(new Color(232, 238, 246));
        photoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        photoLabel.setPreferredSize(new Dimension(150, 150));
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(219, 226, 232), 2));
        if (savedPhotoPath != null && !savedPhotoPath.isEmpty() && new File(savedPhotoPath).exists()) {
            ImageIcon icon = new ImageIcon(savedPhotoPath);
            Image image = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(image));
            photoLabel.setText("");
        }

        JButton uploadPhoto = createActionButton("Upload Photo", new Color(52, 152, 219));
        uploadPhoto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uploadPhoto.setMaximumSize(new Dimension(150, 34));
        uploadPhoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                savedPhotoPath = file.getAbsolutePath();
                try {
                    Files.writeString(Paths.get(PROFILE_PHOTO_PATH_FILE), savedPhotoPath, StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                ImageIcon icon = new ImageIcon(savedPhotoPath);
                Image image = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                photoLabel.setIcon(new ImageIcon(image));
                photoLabel.setText("");
            }
        });

        JPanel photoBox = new JPanel();
        photoBox.setOpaque(false);
        photoBox.setLayout(new BoxLayout(photoBox, BoxLayout.Y_AXIS));
        photoBox.add(photoLabel);
        photoBox.add(Box.createVerticalStrut(12));
        uploadPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoBox.add(uploadPhoto);
        headerRow.add(photoBox, BorderLayout.WEST);

        JPanel profileInfoCard = new JPanel(new GridLayout(7, 1, 10, 10));
        profileInfoCard.setOpaque(false);
        profileInfoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Profile Information"),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        profileInfoCard.add(createFieldPanel("Name", "Ashutosh Kumar Maurya"));
        profileInfoCard.add(createFieldPanel("Role", "Super Admin"));
        profileInfoCard.add(createFieldPanel("Admin ID", "ADM001"));
        profileInfoCard.add(createFieldPanel("Status", "Active"));
        profileInfoCard.add(createFieldPanel("Username", "Ashu1363"));
        profileInfoCard.add(createFieldPanel("Last Login", "28 June 2026, 09:45 AM"));
        profileInfoCard.add(createFieldPanel("Login History", "3 sessions"));
        headerRow.add(profileInfoCard, BorderLayout.CENTER);

        card.add(headerRow, BorderLayout.NORTH);

        JPanel detailsCard = new JPanel(new BorderLayout(0, 12));
        detailsCard.setOpaque(false);
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Administrator Details"),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JPanel detailsGrid = new JPanel(new GridLayout(3, 2, 18, 12));
        detailsGrid.setOpaque(false);
        detailsGrid.add(createFieldPanel("Mobile", "6207159208"));
        detailsGrid.add(createFieldPanel("Email", "ashutoshsingh1363@gmail.com"));
        detailsGrid.add(createFieldPanel("Address", "vill- kripalpu, post- deohalia"));
        detailsGrid.add(createFieldPanel("Dairy Name", "sabarkhanth dairy"));
        detailsGrid.add(createFieldPanel("Center Name", "kripalpur"));
        detailsGrid.add(createFieldPanel("GST Registration No.", "null"));

        detailsCard.add(detailsGrid, BorderLayout.CENTER);
        card.add(detailsCard, BorderLayout.CENTER);

        JPanel bottomGrid = new JPanel(new GridLayout(2, 2, 18, 18));
        bottomGrid.setOpaque(false);
        bottomGrid.add(createSecuritySection());
        bottomGrid.add(createPreferencesSection());
        bottomGrid.add(createActivitySection());
        bottomGrid.add(createBackupSection());
        card.add(bottomGrid, BorderLayout.SOUTH);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);
        JButton editButton = createActionButton("Edit Profile", new Color(52, 152, 219));
        JButton saveButton = createActionButton("Save", new Color(39, 174, 96));
        JButton logoutButton = createActionButton("Logout", new Color(192, 57, 43));
        editButton.setForeground(Color.BLACK);
        saveButton.setForeground(Color.BLACK);
        logoutButton.setForeground(Color.BLACK);
        actionRow.add(editButton);
        actionRow.add(saveButton);
        actionRow.add(logoutButton);
        card.add(actionRow, BorderLayout.PAGE_END);

        profilePanel.add(card, BorderLayout.CENTER);

        editButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Edit mode enabled. Update fields and press Save."));
        saveButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Profile saved successfully."));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        return profilePanel;
    }

    private JPanel createFieldPanel(String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel label = new JLabel(labelText + ":");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.BLACK);
        label.setPreferredSize(new Dimension(160, 24));

        JLabel value = new JLabel(valueText);
        value.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        value.setForeground(Color.BLACK);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private JPanel createSecuritySection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Security"),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        section.add(createFieldPanel("Username", "Ashu1363"));
        section.add(Box.createVerticalStrut(8));
        section.add(createFieldPanel("Last Login", "28 June 2026, 09:45 AM"));
        section.add(Box.createVerticalStrut(8));
        section.add(createFieldPanel("Login History", "3 sessions"));
        return section;
    }

    private JPanel createPreferencesSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Preferences"),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JRadioButton lightMode = new JRadioButton("Light Mode");
        JRadioButton darkMode = new JRadioButton("Dark Mode");
        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lightMode);
        themeGroup.add(darkMode);
        lightMode.setSelected(true);

        JCheckBox notifications = new JCheckBox("Notification");
        notifications.setSelected(true);

        JComboBox<String> language = new JComboBox<>(new String[]{"English", "Hindi"});
        language.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lightMode.addActionListener(e -> applyTheme(false));
        darkMode.addActionListener(e -> applyTheme(true));
        notifications.addActionListener(e -> JOptionPane.showMessageDialog(this, notifications.isSelected() ? "Notifications enabled." : "Notifications disabled."));
        language.addActionListener(e -> JOptionPane.showMessageDialog(this, "Language set to " + language.getSelectedItem()));

        section.add(lightMode);
        section.add(darkMode);
        section.add(Box.createVerticalStrut(8));
        section.add(notifications);
        section.add(Box.createVerticalStrut(8));
        section.add(new JLabel("Language"));
        section.add(language);
        return section;
    }

    private JPanel createActivitySection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Today's Activity"),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        section.add(createFieldPanel("Farmers Added", "0"));
        section.add(Box.createVerticalStrut(8));
        section.add(createFieldPanel("Milk Collected", "0 L"));
        section.add(Box.createVerticalStrut(8));
        section.add(createFieldPanel("Reports Generated", "0"));
        return section;
    }

    private JPanel createBackupSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Backup Database"),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JButton download = createActionButton("Download Backup", new Color(52, 152, 219));
        JButton restore = createActionButton("Restore Backup", new Color(39, 174, 96));
        download.addActionListener(e -> {
            JFileChooser saver = new JFileChooser();
            saver.setDialogTitle("Save Backup");
            if (saver.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(this, "Backup saved to " + saver.getSelectedFile().getAbsolutePath());
            }
        });
        restore.addActionListener(e -> {
            JFileChooser opener = new JFileChooser();
            if (opener.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(this, "Backup restored from " + opener.getSelectedFile().getAbsolutePath());
            }
        });
        section.add(download);
        section.add(Box.createVerticalStrut(10));
        section.add(restore);
        return section;
    }

    private void applyTheme(boolean darkMode) {
        Color bg = darkMode ? new Color(34, 44, 62) : new Color(245, 247, 250);
        Color fg = darkMode ? Color.WHITE : new Color(18, 76, 140);
        getContentPane().setBackground(bg);
        for (Component comp : getContentPane().getComponents()) {
            comp.setBackground(bg);
            if (comp instanceof JPanel) {
                for (Component child : ((JPanel) comp).getComponents()) {
                    child.setBackground(bg);
                    if (child instanceof JLabel) {
                        child.setForeground(fg);
                    }
                }
            }
        }
        repaint();
    }

    private JPanel createSectionCard(String title, String[][] rows) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(250, 251, 253));
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 232)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(new Color(18, 76, 140));
        section.add(header);
        section.add(Box.createVerticalStrut(12));

        for (String[] row : rows) {
            if (row[1].isEmpty()) {
                JLabel label = new JLabel(row[0]);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                label.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
                section.add(label);
            } else {
                section.add(createInfoLabel(row[0], row[1], true));
            }
        }
        return section;
    }

    private JPanel createInfoLabel(String label, String value, boolean small) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel left = new JLabel(label);
        left.setFont(new Font("Segoe UI", Font.BOLD, small ? 13 : 14));
        left.setForeground(Color.BLACK);
        JLabel right = new JLabel(value);
        right.setFont(new Font("Segoe UI", Font.PLAIN, small ? 13 : 14));
        right.setForeground(Color.BLACK);
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.CENTER);
        return row;
    }

    private JButton createActionButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return button;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color cardColor) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(cardColor);
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        card.add(titleLabel, BorderLayout.NORTH);

        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    //================ LOAD DASHBOARD =================

    private void loadDashboardData() {
        try {
            MilkDAO.DashboardSummary summary = new MilkDAO().getTodaySummary();
            lblTodayMilk.setText(summary.getTotalMilk() + " Ltr");
            lblTodayAmount.setText("Rs. " + String.format("%.2f", summary.getTotalAmount()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadSavedProfilePhoto() {
        File file = new File(PROFILE_PHOTO_PATH_FILE);
        if (file.exists()) {
            try {
                savedPhotoPath = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}