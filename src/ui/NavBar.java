package ui;

import model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class NavBar extends JPanel {

    public interface NavListener {
        void onNavClick(String section);
    }

    private final NavListener listener;
    private final String[]    navItems;
    private       String      activeItem;
    private final JLabel[]    navLabels;

    public NavBar(User user, String[] navItems, String activeItem,
                  NavListener listener) {
        this.navItems   = navItems;
        this.activeItem = activeItem;
        this.listener   = listener;
        this.navLabels  = new JLabel[navItems.length];

        setBackground(Theme.BG_CARD);
        setPreferredSize(new Dimension(220, getHeight()));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));

        // ── TOP: Brand ──
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(28, 20, 20, 20));

        JLabel logo = new JLabel("🏗️ NIRIKSHAN");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Theme.TEXT_PRIMARY);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleTag = makeRoleTag(user.getRole());
        roleTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        brandPanel.add(logo);
        brandPanel.add(Box.createVerticalStrut(10));
        brandPanel.add(roleTag);
        brandPanel.add(Box.createVerticalStrut(20));
        brandPanel.add(sep);

        // ── MIDDLE: Nav Items ──
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] icons = getIconsForRole(user.getRole());

        for (int i = 0; i < navItems.length; i++) {
            JLabel item = createNavItem(icons[i], navItems[i]);
            navLabels[i] = item;
            if (navItems[i].equals(activeItem)) setActive(item);
            navPanel.add(item);
            navPanel.add(Box.createVerticalStrut(4));
        }

        // ── BOTTOM: User info + Logout ──
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(12, 12, 20, 12));

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(Theme.BORDER);
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // User info card
        RoundedPanel userCard = new RoundedPanel(10, Theme.BG_INPUT);
        userCard.setLayout(new BoxLayout(userCard, BoxLayout.Y_AXIS));
        userCard.setBorder(new EmptyBorder(10, 12, 10, 12));
        userCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel userName = new JLabel("👤 " + user.getName());
        userName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userName.setForeground(Theme.TEXT_PRIMARY);

        JLabel userEmail = new JLabel(user.getEmail());
        userEmail.setFont(Theme.FONT_SMALL);
        userEmail.setForeground(Theme.TEXT_SECONDARY);

        userCard.add(userName);
        userCard.add(Box.createVerticalStrut(4));
        userCard.add(userEmail);
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Logout button
        JButtonStyled logoutBtn = Theme.dangerButton("⏻  Logout");
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
            new LoginScreen();
        });

        bottomPanel.add(sep2);
        bottomPanel.add(Box.createVerticalStrut(12));
        bottomPanel.add(userCard);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(logoutBtn);

        add(brandPanel, BorderLayout.NORTH);
        add(navPanel,   BorderLayout.CENTER);
        add(bottomPanel,BorderLayout.SOUTH);
    }

    // Create one nav item label
    private JLabel createNavItem(String icon, String text) {
        JLabel label = new JLabel(icon + "  " + text);
        label.setFont(Theme.FONT_NAV);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setBorder(new EmptyBorder(10, 14, 10, 14));
        label.setOpaque(false);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        label.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!label.getText().contains(activeItem))
                    label.setForeground(Theme.TEXT_PRIMARY);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!label.getText().contains(activeItem))
                    label.setForeground(Theme.TEXT_SECONDARY);
            }
            @Override public void mouseClicked(MouseEvent e) {
                setActiveItem(text);
                listener.onNavClick(text);
            }
        });
        return label;
    }

    public void setActiveItem(String item) {
        this.activeItem = item;
        for (int i = 0; i < navItems.length; i++) {
            if (navItems[i].equals(item)) {
                setActive(navLabels[i]);
            } else {
                setInactive(navLabels[i]);
            }
        }
    }

    private void setActive(JLabel label) {
        label.setForeground(Theme.PRIMARY);
        label.setOpaque(true);
        label.setBackground(Theme.PRIMARY_LIGHT);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.PRIMARY),
            new EmptyBorder(10, 11, 10, 14)
        ));
    }

    private void setInactive(JLabel label) {
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setOpaque(false);
        label.setBorder(new EmptyBorder(10, 14, 10, 14));
    }

    private JLabel makeRoleTag(String role) {
        JLabel tag = new JLabel("  " + role + "  ");
        tag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tag.setForeground(Color.WHITE);
        tag.setBackground(Theme.PRIMARY);
        tag.setOpaque(true);
        tag.setBorder(new EmptyBorder(3, 6, 3, 6));
        return tag;
    }

    private String[] getIconsForRole(String role) {
        return switch (role) {
            case "ADMIN"     -> new String[]{"📊","📁","🏁","💳","📋"};
            case "CONTRACTOR" -> new String[]{"📊","📁","🏁"}; 
            case "INSPECTOR" -> new String[]{"📊","🔍","📋"};
            default          -> new String[]{"📊","💬"};
        };
    }
}