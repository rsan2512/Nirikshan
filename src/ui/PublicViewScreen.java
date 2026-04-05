package ui;

import dao.FeedbackDAO;
import dao.FeedbackDAOImpl;
import dao.ProjectDAO;
import model.Project;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PublicViewScreen extends JFrame {

    private final User        user;
    private final ProjectDAO  projectDAO  = new ProjectDAO();
    private final FeedbackDAOImpl feedbackDAO = new FeedbackDAOImpl();
    private JPanel contentArea;

    private static final String[] NAV_ITEMS = {"Dashboard", "Feedback"};

    public PublicViewScreen(User user) {
        this.user = user;

        setTitle("Nirikshan — Public Portal");
        setSize(1200, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_MAIN);
        setContentPane(root);

        NavBar navBar = new NavBar(user, NAV_ITEMS, "Dashboard",
                section -> showSection(section));
        root.add(navBar, BorderLayout.WEST);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Theme.BG_MAIN);
        root.add(contentArea, BorderLayout.CENTER);

        showSection("Dashboard");
        setVisible(true);
    }

    private void showSection(String section) {
        contentArea.removeAll();
        switch (section) {
            case "Dashboard" -> contentArea.add(buildDashboard(), BorderLayout.CENTER);
            case "Feedback"  -> contentArea.add(buildFeedback(),  BorderLayout.CENTER);
        }
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ══════════════════════════════════════════
    // SECTION 1 — Public Project Dashboard
    // ══════════════════════════════════════════
    private JPanel buildDashboard() {
        JPanel panel = basePanel();

        panel.add(pageTitle("Public Projects",
                "Track government project progress transparently"));
        panel.add(Box.createVerticalStrut(20));

        List<Project> projects = projectDAO.getAllProjects();

        if (projects.isEmpty()) {
            JLabel empty = new JLabel("No projects found.");
            empty.setFont(Theme.FONT_SUBTITLE);
            empty.setForeground(Theme.TEXT_SECONDARY);
            panel.add(empty);
            return panel;
        }

        JPanel cardsGrid = new JPanel(new GridLayout(0, 2, 16, 16));
        cardsGrid.setOpaque(false);

        for (Project p : projects) {
            cardsGrid.add(buildProjectCard(p));
        }

        JScrollPane scroll = new JScrollPane(cardsGrid);
        scroll.setBackground(Theme.BG_MAIN);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(scroll);
        return panel;
    }

    private RoundedPanel buildProjectCard(Project p) {
        RoundedPanel card = new RoundedPanel(14, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Status badge
        Color statusColor = switch (p.getStatus()) {
            case "ACTIVE"     -> Theme.SUCCESS;
            case "SUSPENDED"  -> Theme.DANGER;
            default           -> Theme.TEXT_SECONDARY;
        };
        JLabel statusBadge = new JLabel("  " + p.getStatus() + "  ");
        statusBadge.setFont(Theme.FONT_SMALL);
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setBackground(statusColor);
        statusBadge.setOpaque(true);
        statusBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        statusBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Project name
        JLabel name = new JLabel(p.getName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Location
        JLabel location = new JLabel("📍 " + p.getLocation());
        location.setFont(Theme.FONT_SUBTITLE);
        location.setForeground(Theme.TEXT_SECONDARY);
        location.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Budget
        JLabel budget = new JLabel("💰 ₹ "
                + String.format("%,.2f", p.getTotalBudget()));
        budget.setFont(Theme.FONT_SUBTITLE);
        budget.setForeground(Theme.TEXT_SECONDARY);
        budget.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Average rating
        double avgRating = feedbackDAO.getAverageRating(p.getProjectId());
        String stars = "⭐".repeat((int) Math.round(avgRating));
        JLabel ratingLabel = new JLabel(stars.isEmpty()
                ? "No ratings yet" : stars + String.format(" (%.1f/5)", avgRating));
        ratingLabel.setFont(Theme.FONT_SUBTITLE);
        ratingLabel.setForeground(Theme.WARNING);
        ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(statusBadge);
        card.add(Box.createVerticalStrut(10));
        card.add(name);
        card.add(Box.createVerticalStrut(6));
        card.add(location);
        card.add(Box.createVerticalStrut(4));
        card.add(budget);
        card.add(Box.createVerticalStrut(8));
        card.add(ratingLabel);
        return card;
    }

    // ══════════════════════════════════════════
    // SECTION 2 — Submit Feedback
    // ══════════════════════════════════════════
    private JPanel buildFeedback() {
        JPanel panel = basePanel();

        panel.add(pageTitle("Submit Feedback",
                "Rate project quality — your rating affects payment release"));
        panel.add(Box.createVerticalStrut(24));

        RoundedPanel card = new RoundedPanel(16, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 28, 28, 28));
        card.setMaximumSize(new Dimension(560, 420));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Project selector
        JLabel projectLabel = formLabel("Select Project");
        List<Project> projects = projectDAO.getAllProjects();
        JComboBox<Project> projectBox =
                new JComboBox<>(projects.toArray(new Project[0]));
        projectBox.setFont(Theme.FONT_INPUT);
        projectBox.setBackground(Theme.BG_INPUT);
        projectBox.setForeground(Theme.TEXT_PRIMARY);
        projectBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        projectBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Star rating selector
        JLabel ratingLabel = formLabel("Your Rating");
        JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        starsPanel.setOpaque(false);
        starsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup starGroup = new ButtonGroup();
        JRadioButton[] stars  = new JRadioButton[5];
        for (int i = 0; i < 5; i++) {
            stars[i] = new JRadioButton("⭐ " + (i + 1));
            stars[i].setFont(Theme.FONT_INPUT);
            stars[i].setForeground(Theme.TEXT_PRIMARY);
            stars[i].setBackground(Theme.BG_CARD);
            stars[i].setOpaque(false);
            stars[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            starGroup.add(stars[i]);
            starsPanel.add(stars[i]);
        }
        stars[2].setSelected(true); // default 3 stars

        // Comment field
        JLabel commentLabel = formLabel("Comment (Optional)");
        JTextArea commentField = new JTextArea(3, 20);
        commentField.setFont(Theme.FONT_INPUT);
        commentField.setBackground(Theme.BG_INPUT);
        commentField.setForeground(Theme.TEXT_PRIMARY);
        commentField.setCaretColor(Theme.TEXT_PRIMARY);
        commentField.setBorder(new EmptyBorder(10, 12, 10, 12));
        commentField.setLineWrap(true);
        commentField.setWrapStyleWord(true);
        commentField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane commentScroll = new JScrollPane(commentField);
        commentScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        commentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        // Impact notice
        RoundedPanel noticeCard = new RoundedPanel(10, Theme.PRIMARY_LIGHT);
        noticeCard.setLayout(new BoxLayout(noticeCard, BoxLayout.Y_AXIS));
        noticeCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        noticeCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        noticeCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noticeText = new JLabel(
                "⚡  Ratings below 3★ will block payment release for this project.");
        noticeText.setFont(Theme.FONT_SMALL);
        noticeText.setForeground(Theme.PRIMARY_DARK);
        noticeCard.add(noticeText);

        // Submit button
        JButtonStyled submitBtn = Theme.primaryButton("Submit Feedback");
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        submitBtn.addActionListener(e -> {
            Project selected = (Project) projectBox.getSelectedItem();
            if (selected == null) return;

            int rating = 3;
            for (int i = 0; i < 5; i++) {
                if (stars[i].isSelected()) { rating = i + 1; break; }
            }

            String comment = commentField.getText().trim();
            boolean ok = feedbackDAO.addFeedback(
                    selected.getProjectId(), rating, comment);

            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Thank you! Your feedback has been recorded.",
                        "✅ Feedback Submitted",
                        JOptionPane.INFORMATION_MESSAGE);
                commentField.setText("");
                stars[2].setSelected(true);
                showSection("Dashboard");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to submit feedback. Try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(projectLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(projectBox);
        card.add(Box.createVerticalStrut(16));
        card.add(ratingLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(starsPanel);
        card.add(Box.createVerticalStrut(16));
        card.add(commentLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(commentScroll);
        card.add(Box.createVerticalStrut(14));
        card.add(noticeCard);
        card.add(Box.createVerticalStrut(20));
        card.add(submitBtn);

        panel.add(card);
        return panel;
    }

    // ══════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════
    private JPanel basePanel() {
        JPanel p = new JPanel();
        p.setBackground(Theme.BG_MAIN);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(30, 30, 30, 30));
        return p;
    }

    private JPanel pageTitle(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_TITLE);
        t.setForeground(Theme.TEXT_PRIMARY);
        JLabel s = new JLabel(subtitle);
        s.setFont(Theme.FONT_SUBTITLE);
        s.setForeground(Theme.TEXT_SECONDARY);
        p.add(t); p.add(s);
        return p;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}