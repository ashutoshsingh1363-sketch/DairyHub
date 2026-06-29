package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import dao.UserDAO;
import model.User;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private final JTextField txtUser;
    private final JPasswordField txtPass;
    private final JButton btnLogin;
    private final JButton btnExit;
    private final JCheckBox showPassword;
    private final char defaultEchoChar;

    public LoginFrame() {
        setTitle("DAIRY HUB - Login");
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setBackground(new Color(238, 242, 246));
        container.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setContentPane(container);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(18, 76, 140));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(14, 55, 102)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel title = new JLabel("DAIRY HUB");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerPanel.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Milk Collection Management System");
        subtitle.setForeground(new Color(193, 214, 244));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        container.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 208, 216)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 6, 10, 6);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(lblUser, gbc);

        gbc.gridy++;
        txtUser = new JTextField();
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUser.setPreferredSize(new Dimension(280, 34));
        txtUser.setBorder(BorderFactory.createLineBorder(new Color(190, 200, 210)));
        formPanel.add(txtUser, gbc);

        gbc.gridy++;
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(lblPass, gbc);

        gbc.gridy++;
        txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setPreferredSize(new Dimension(280, 34));
        txtPass.setBorder(BorderFactory.createLineBorder(new Color(190, 200, 210)));
        formPanel.add(txtPass, gbc);

        defaultEchoChar = txtPass.getEchoChar();

        gbc.gridy++;
        showPassword = new JCheckBox("Show Password");
        showPassword.setBackground(Color.WHITE);
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(showPassword, gbc);

        gbc.gridy++;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnLogin = createButton("Login", new Color(18, 76, 140), Color.BLACK);
        btnExit = createButton("Exit", new Color(149, 165, 166), Color.BLACK);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);
        formPanel.add(buttonPanel, gbc);

        container.add(formPanel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> attemptLogin());
        btnExit.addActionListener(e -> System.exit(0));
        showPassword.addActionListener(e -> txtPass.setEchoChar(showPassword.isSelected() ? (char) 0 : defaultEchoChar));

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void attemptLogin() {
        String username = txtUser.getText().trim();
        String password = String.valueOf(txtPass.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Missing Credentials", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            Optional<User> user = userDAO.authenticate(username, password);
            if (user.isPresent()) {
                User loggedInUser = user.get();
                JOptionPane.showMessageDialog(this, loggedInUser.getRole() + " login successful!", "Welcome", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new Dashboard(loggedInUser.getRole(), loggedInUser.getDisplayName());
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}