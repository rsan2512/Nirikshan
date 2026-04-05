package ui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginScreen extends JFrame {

    private JTextField     emailField;
    private JPasswordField passwordField;

    public LoginScreen() {
        setTitle("Nirikshan — Login");
        setSize(1000, 620);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Root panel ──
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(Theme.BG_MAIN);
        setContentPane(root);

        // ── LEFT PANEL — Branding ──
        JPanel left = new JPanel();
        left.setBackground(Theme.PRIMARY);
        left.setLayout(new GridBagLayout());

        JPanel brandBox = new JPanel();
        brandBox.setOpaque(false);
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setBorder(new EmptyBorder(0, 40, 0, 40));

        // Icon circle
        JLabel iconLabel = new JLabel("🏗️", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // App name
        JLabel appName = new JLabel("Nirikshan");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 32));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tagline
        JLabel tagline = new JLabel("<html><div style='text-align:center'>"
                + "Public Work Quality &<br>Payment Verification System"
                + "</div></html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tagline.setForeground(new Color(255, 255, 255, 180));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 60));
        sep.setMaximumSize(new Dimension(200, 1));

        // Features list
        String[] features = {
            "✅  Milestone-based payments",
            "✅  Multi-level verification",
            "✅  Public feedback gate",
            "✅  Permanent audit trail"
        };

        JPanel featuresPanel = new JPanel();
        featuresPanel.setOpaque(false);
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (String f : features) {
            JLabel fl = new JLabel(f);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fl.setForeground(new Color(255, 255, 255, 200));
            fl.setBorder(new EmptyBorder(4, 0, 4, 0));
            featuresPanel.add(fl);
        }

        brandBox.add(iconLabel);
        brandBox.add(Box.createVerticalStrut(16));
        brandBox.add(appName);
        brandBox.add(Box.createVerticalStrut(8));
        brandBox.add(tagline);
        brandBox.add(Box.createVerticalStrut(24));
        brandBox.add(sep);
        brandBox.add(Box.createVerticalStrut(24));
        brandBox.add(featuresPanel);

        left.add(brandBox);

        // ── RIGHT PANEL — Login Form ──
        JPanel right = new JPanel();
        right.setBackground(Theme.BG_MAIN);
        right.setLayout(new GridBagLayout());

        RoundedPanel card = new RoundedPanel(20, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(380, 440));

        // Welcome text
        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(Theme.FONT_TITLE);
        welcome.setForeground(Theme.TEXT_PRIMARY);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(Theme.FONT_SUBTITLE);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(Theme.FONT_LABEL);
        emailLabel.setForeground(Theme.TEXT_SECONDARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailField = new JTextField();
        emailField.setFont(Theme.FONT_INPUT);
        emailField.setBackground(Theme.BG_INPUT);
        emailField.setForeground(Theme.TEXT_PRIMARY);
        emailField.setCaretColor(Theme.TEXT_PRIMARY);
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_LABEL);
        passLabel.setForeground(Theme.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(Theme.FONT_INPUT);
        passwordField.setBackground(Theme.BG_INPUT);
        passwordField.setForeground(Theme.TEXT_PRIMARY);
        passwordField.setCaretColor(Theme.TEXT_PRIMARY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login button
        JButtonStyled loginBtn = Theme.primaryButton("Sign In →");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Enter key triggers login
        passwordField.addActionListener(e -> handleLogin(errorLabel));
        loginBtn.addActionListener(e -> handleLogin(errorLabel));

        // Assemble card
        card.add(welcome);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(32));
        card.add(emailLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(16));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(20));

        right.add(card);

        // Assemble root
        root.add(left);
        root.add(right);

        setVisible(true);
    }

    private void handleLogin(JLabel errorLabel) {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("⚠ Please enter email and password.");
            return;
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.login(email, password);

        if (user == null) {
            errorLabel.setText("❌ Invalid email or password.");
            passwordField.setText("");
            return;
        }

        dispose();

        // Route to correct dashboard by role
        switch (user.getRole()) {
            case "ADMIN"       -> new AdminDashboard(user);
            case "CONTRACTOR"  -> new ContractorDashboard(user);  // ← add this
            case "INSPECTOR"   -> new InspectorDashboard(user);
            default            -> new PublicViewScreen(user);
    }
    }

    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        // Smoother rendering on Windows
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        new LoginScreen();
    });
    }
}