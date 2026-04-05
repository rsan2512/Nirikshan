package ui;

import dao.MilestoneDAO;
import dao.ProjectDAO;
import model.Milestone;
import model.Project;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class ContractorDashboard extends JFrame {

    private final User         user;
    private final ProjectDAO   projectDAO   = new ProjectDAO();
    private final MilestoneDAO milestoneDAO = new MilestoneDAO();

    private JPanel contentArea;

    private static final String[] NAV_ITEMS = {
        "Dashboard", "My Projects", "My Milestones"
    };

    public ContractorDashboard(User user) {
        this.user = user;

        setTitle("Nirikshan — Contractor");
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
    private JComboBox<User> styledUserComboBox(User[] items) {
    JComboBox<User> box = new JComboBox<>(items);
    box.setFont(Theme.FONT_INPUT);
    box.setBackground(Theme.BG_INPUT);
    box.setForeground(Theme.TEXT_PRIMARY);
    box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    box.setAlignmentX(Component.LEFT_ALIGNMENT);

    box.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            setText(value == null ? "" : value.toString());
            setBackground(isSelected ? Theme.PRIMARY : Theme.BG_INPUT);
            setForeground(isSelected ? Color.WHITE   : Theme.TEXT_PRIMARY);
            setFont(Theme.FONT_INPUT);
            setBorder(new EmptyBorder(8, 12, 8, 12));
            return this;
        }
    });
    return box;
}
    private JComboBox<Project> styledComboBox(Project[] items) {
    JComboBox<Project> box = new JComboBox<>(items);
    box.setFont(Theme.FONT_INPUT);
    box.setBackground(Theme.BG_INPUT);
    box.setForeground(Theme.TEXT_PRIMARY);
    box.setPreferredSize(new Dimension(280, 38));

    // Fix selected item visibility
    box.setOpaque(true);
    UIManager.put("ComboBox.selectionBackground", Theme.PRIMARY);
    UIManager.put("ComboBox.selectionForeground", Theme.TEXT_PRIMARY);
    UIManager.put("ComboBox.background",          Theme.BG_INPUT);
    UIManager.put("ComboBox.foreground",          Theme.TEXT_PRIMARY);

    box.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            setText(value == null ? "" : value.toString());
            setBackground(isSelected ? Theme.PRIMARY   : Theme.BG_INPUT);
            setForeground(isSelected ? Color.WHITE     : Theme.TEXT_PRIMARY);
            setFont(Theme.FONT_INPUT);
            setBorder(new EmptyBorder(8, 12, 8, 12));
            return this;
        }
    });
    return box;
}
    private void showSection(String section) {
        contentArea.removeAll();
        switch (section) {
            case "Dashboard"     -> contentArea.add(buildDashboard(),    BorderLayout.CENTER);
            case "My Projects"   -> contentArea.add(buildMyProjects(),   BorderLayout.CENTER);
            case "My Milestones" -> contentArea.add(buildMyMilestones(), BorderLayout.CENTER);
        }
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ══════════════════════════════════════════
    // SECTION 1 — Dashboard
    // ══════════════════════════════════════════
    private JPanel buildDashboard() {
        JPanel panel = basePanel();

        panel.add(pageTitle("Contractor Dashboard",
                "Welcome, " + user.getName()));
        panel.add(Box.createVerticalStrut(24));

        // Stats
        List<Project> myProjects = projectDAO.getProjectsByContractor(user.getUserId());

        long active    = myProjects.stream()
                .filter(p -> p.getStatus().equals("ACTIVE")).count();
        long completed = myProjects.stream()
                .filter(p -> p.getStatus().equals("COMPLETED")).count();

        // Count all milestones across my projects
        long totalMilestones = 0;
        long paidMilestones  = 0;
        for (Project p : myProjects) {
            List<Milestone> ms = milestoneDAO.getMilestonesByProject(p.getProjectId());
            totalMilestones += ms.size();
            paidMilestones  += ms.stream()
                    .filter(m -> m.getStatus().equals("PAID")).count();
        }

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        statsRow.add(statCard("My Projects",    String.valueOf(myProjects.size()),  "📁", Theme.PRIMARY));
        statsRow.add(statCard("Active",         String.valueOf(active),             "🔄", Theme.WARNING));
        statsRow.add(statCard("Completed",      String.valueOf(completed),          "✅", Theme.SUCCESS));
        statsRow.add(statCard("Paid Milestones",String.valueOf(paidMilestones)
                + "/" + totalMilestones,                                            "💳", Theme.DANGER));

        panel.add(statsRow);
        panel.add(Box.createVerticalStrut(30));

        // Info card
        RoundedPanel infoCard = new RoundedPanel(14, Theme.BG_CARD);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(new EmptyBorder(24, 24, 24, 24));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel infoTitle = new JLabel("⚙️  How Nirikshan Works for You");
        infoTitle.setFont(Theme.FONT_LABEL);
        infoTitle.setForeground(Theme.TEXT_PRIMARY);

        String[] points = {
            "→  Admin creates projects and milestones assigned to you",
            "→  Go to My Milestones to submit completed work",
            "→  Inspector reviews and approves your submission",
            "→  Public rates the project quality",
            "→  Admin releases payment only after both checks pass"
        };

        infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(14));
        for (String pt : points) {
            JLabel l = new JLabel(pt);
            l.setFont(Theme.FONT_SUBTITLE);
            l.setForeground(Theme.TEXT_SECONDARY);
            infoCard.add(l);
            infoCard.add(Box.createVerticalStrut(5));
        }

        panel.add(infoCard);
        panel.add(Box.createVerticalStrut(20));

        // Recent milestones table
        panel.add(sectionLabel("Recent Milestones"));
        panel.add(Box.createVerticalStrut(12));

        // Gather all milestones from all my projects
        List<Milestone> allMilestones = new java.util.ArrayList<>();
        for (Project p : myProjects) {
            allMilestones.addAll(
                milestoneDAO.getMilestonesByProject(p.getProjectId()));
        }

        panel.add(buildMilestoneTable(allMilestones, false));
        return panel;
    }

    // ══════════════════════════════════════════
    // SECTION 2 — My Projects
    // ══════════════════════════════════════════
    private JPanel buildMyProjects() {
        JPanel panel = basePanel();

        panel.add(pageTitle("My Projects",
                "All projects assigned to you"));
        panel.add(Box.createVerticalStrut(20));

        List<Project> myProjects =
                projectDAO.getProjectsByContractor(user.getUserId());

        if (myProjects.isEmpty()) {
            panel.add(emptyState(
                "📁  No projects assigned yet.",
                "Contact your admin to get assigned to a project."));
            return panel;
        }

        // Project cards
        JPanel cardsGrid = new JPanel(new GridLayout(0, 2, 16, 16));
        cardsGrid.setOpaque(false);

        for (Project p : myProjects) {
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
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        // Status badge
        Color statusColor = switch (p.getStatus()) {
            case "ACTIVE"    -> Theme.SUCCESS;
            case "SUSPENDED" -> Theme.DANGER;
            default          -> Theme.TEXT_SECONDARY;
        };

        JLabel badge = new JLabel("  " + p.getStatus() + "  ");
        badge.setFont(Theme.FONT_SMALL);
        badge.setForeground(Color.WHITE);
        badge.setBackground(statusColor);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel(p.getName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel location = new JLabel("📍 " + p.getLocation());
        location.setFont(Theme.FONT_SUBTITLE);
        location.setForeground(Theme.TEXT_SECONDARY);
        location.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel budget = new JLabel("💰 ₹ "
                + String.format("%,.2f", p.getTotalBudget()));
        budget.setFont(Theme.FONT_SUBTITLE);
        budget.setForeground(Theme.TEXT_SECONDARY);
        budget.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Milestone count
        List<Milestone> ms =
                milestoneDAO.getMilestonesByProject(p.getProjectId());
        long paid = ms.stream()
                .filter(m -> m.getStatus().equals("PAID")).count();

        JLabel milestoneCount = new JLabel(
                "🏁 Milestones: " + paid + "/" + ms.size() + " paid");
        milestoneCount.setFont(Theme.FONT_SUBTITLE);
        milestoneCount.setForeground(Theme.SUCCESS);
        milestoneCount.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(badge);
        card.add(Box.createVerticalStrut(10));
        card.add(name);
        card.add(Box.createVerticalStrut(6));
        card.add(location);
        card.add(Box.createVerticalStrut(4));
        card.add(budget);
        card.add(Box.createVerticalStrut(8));
        card.add(milestoneCount);

        return card;
    }

    // ══════════════════════════════════════════
    // SECTION 3 — My Milestones
    // ══════════════════════════════════════════
    private JPanel buildMyMilestones() {
        JPanel panel = basePanel();

        panel.add(pageTitle("My Milestones",
                "Submit completed milestones for inspection"));
        panel.add(Box.createVerticalStrut(20));

        List<Project> myProjects =
                projectDAO.getProjectsByContractor(user.getUserId());

        if (myProjects.isEmpty()) {
            panel.add(emptyState(
                "📁  No projects yet.",
                "You have not been assigned to any project."));
            return panel;
        }

        // Project selector
        JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        selectorRow.setOpaque(false);
        selectorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel selectLabel = new JLabel("Select Project:  ");
        selectLabel.setFont(Theme.FONT_LABEL);
        selectLabel.setForeground(Theme.TEXT_SECONDARY);

        JComboBox<Project> projectBox =
                new JComboBox<>(myProjects.toArray(new Project[0]));
        projectBox.setFont(Theme.FONT_INPUT);
        projectBox.setBackground(Theme.BG_INPUT);
        projectBox.setForeground(Theme.TEXT_PRIMARY);
        projectBox.setPreferredSize(new Dimension(280, 38));

        selectorRow.add(selectLabel);
        selectorRow.add(projectBox);

        // Milestone cards holder
        JPanel cardsHolder = new JPanel();
        cardsHolder.setOpaque(false);
        cardsHolder.setLayout(new BoxLayout(cardsHolder, BoxLayout.Y_AXIS));

        Runnable refreshCards = () -> {
            cardsHolder.removeAll();
            Project selected = (Project) projectBox.getSelectedItem();
            if (selected == null) return;

            List<Milestone> milestones =
                    milestoneDAO.getMilestonesByProject(selected.getProjectId());

            if (milestones.isEmpty()) {
                cardsHolder.add(emptyState(
                    "🏁  No milestones yet.",
                    "Admin has not added milestones to this project."));
            } else {
                for (Milestone m : milestones) {
                    cardsHolder.add(buildMilestoneSubmitCard(m));
                    cardsHolder.add(Box.createVerticalStrut(10));
                }
            }
            cardsHolder.revalidate();
            cardsHolder.repaint();
        };

        projectBox.addActionListener(e -> refreshCards.run());

        panel.add(selectorRow);
        panel.add(Box.createVerticalStrut(20));

        JScrollPane scroll = new JScrollPane(cardsHolder);
        scroll.setBackground(Theme.BG_MAIN);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(scroll);

        // Load first project milestones
        refreshCards.run();

        return panel;
    }

    private RoundedPanel buildMilestoneSubmitCard(Milestone m) {
        RoundedPanel card = new RoundedPanel(14, Theme.BG_CARD);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Left — info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel desc = new JLabel("Milestone #" + m.getMilestoneId()
                + "  —  " + m.getDescription());
        desc.setFont(Theme.FONT_LABEL);
        desc.setForeground(Theme.TEXT_PRIMARY);

        JLabel amount = new JLabel("₹ " + String.format("%,.2f", m.getBudget()));
        amount.setFont(Theme.FONT_SUBTITLE);
        amount.setForeground(Theme.TEXT_SECONDARY);

        // Status badge
        Color badgeColor = switch (m.getStatus()) {
            case "PENDING"   -> Theme.TEXT_SECONDARY;
            case "SUBMITTED" -> Theme.WARNING;
            case "APPROVED"  -> Theme.SUCCESS;
            case "REJECTED"  -> Theme.DANGER;
            case "PAID"      -> Theme.PRIMARY;
            default          -> Theme.TEXT_SECONDARY;
        };

        JLabel statusBadge = new JLabel("  " + m.getStatus() + "  ");
        statusBadge.setFont(Theme.FONT_SMALL);
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setBackground(badgeColor);
        statusBadge.setOpaque(true);
        statusBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        info.add(desc);
        info.add(Box.createVerticalStrut(5));
        info.add(amount);
        info.add(Box.createVerticalStrut(5));
        info.add(statusBadge);

        // Right — submit button (only if PENDING or REJECTED)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        if (m.getStatus().equals("PENDING") || m.getStatus().equals("REJECTED")) {
            JButtonStyled submitBtn = Theme.primaryButton("📤 Submit");
            submitBtn.setPreferredSize(new Dimension(130, 38));
            submitBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Submit Milestone #" + m.getMilestoneId()
                        + " for inspection?\n\n\""
                        + m.getDescription() + "\"",
                        "Confirm Submission",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = milestoneDAO.submitMilestone(m.getMilestoneId());
                    if (ok) {
                        JOptionPane.showMessageDialog(this,
                                "✅ Milestone submitted for inspection!\n"
                                + "Inspector will review it shortly.",
                                "Submitted",
                                JOptionPane.INFORMATION_MESSAGE);
                        showSection("My Milestones");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Failed to submit. Try again.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            actions.add(submitBtn);

        } else if (m.getStatus().equals("SUBMITTED")) {
            JLabel waiting = new JLabel("⏳ Awaiting inspection");
            waiting.setFont(Theme.FONT_SMALL);
            waiting.setForeground(Theme.WARNING);
            actions.add(waiting);

        } else if (m.getStatus().equals("PAID")) {
            JLabel paid = new JLabel("💳 Payment received");
            paid.setFont(Theme.FONT_SMALL);
            paid.setForeground(Theme.SUCCESS);
            actions.add(paid);
        }

        card.add(info,    BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    // ══════════════════════════════════════════
    // SHARED TABLE BUILDER
    // ══════════════════════════════════════════
    private JScrollPane buildMilestoneTable(List<Milestone> milestones,
                                             boolean showSubmit) {
        String[] cols = {"ID", "Description", "Amount (₹)", "Status"};
        Object[][] data = new Object[milestones.size()][4];
        for (int i = 0; i < milestones.size(); i++) {
            Milestone m = milestones.get(i);
            data[i] = new Object[]{
                m.getMilestoneId(),
                m.getDescription(),
                String.format("%,.2f", m.getBudget()),
                m.getStatus()
            };
        }

        JTable table = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setBackground(Theme.BG_CARD);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(Theme.BG_INPUT);
        table.getTableHeader().setForeground(Theme.TEXT_SECONDARY);
        table.getTableHeader().setFont(Theme.FONT_NAV);
        table.setSelectionBackground(Theme.PRIMARY_LIGHT);
        table.setSelectionForeground(Theme.PRIMARY_DARK);

        // Color status column
        table.getColumnModel().getColumn(3)
                .setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                String v = val == null ? "" : val.toString();
                setForeground(switch (v) {
                    case "PAID"      -> Theme.PRIMARY;
                    case "APPROVED"  -> Theme.SUCCESS;
                    case "SUBMITTED" -> Theme.WARNING;
                    case "REJECTED"  -> Theme.DANGER;
                    default          -> Theme.TEXT_SECONDARY;
                });
                setBackground(sel ? Theme.PRIMARY_LIGHT :
                        (row % 2 == 0 ? Theme.BG_CARD : Theme.BG_INPUT));
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        // Alternating rows for other columns
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                setBackground(sel ? Theme.PRIMARY_LIGHT :
                        (row % 2 == 0 ? Theme.BG_CARD : Theme.BG_INPUT));
                setForeground(sel ? Theme.PRIMARY_DARK : Theme.TEXT_PRIMARY);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_CARD);
        scroll.getViewport().setBackground(Theme.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return scroll;
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

    private RoundedPanel statCard(String label, String value,
                                   String icon, Color color) {
        RoundedPanel card = new RoundedPanel(14, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel iconLbl = new JLabel(icon + "  " + label);
        iconLbl.setFont(Theme.FONT_SMALL);
        iconLbl.setForeground(Theme.TEXT_SECONDARY);
        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLbl.setForeground(color);
        card.add(iconLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLbl);
        return card;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private RoundedPanel emptyState(String title, String subtitle) {
        RoundedPanel card = new RoundedPanel(14, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_LABEL);
        t.setForeground(Theme.TEXT_PRIMARY);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel s = new JLabel(subtitle);
        s.setFont(Theme.FONT_SUBTITLE);
        s.setForeground(Theme.TEXT_SECONDARY);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(t);
        card.add(Box.createVerticalStrut(6));
        card.add(s);
        return card;
    }
}