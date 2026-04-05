package ui;

import dao.InspectionDAO;
import dao.InspectionDAOImpl;
import dao.MilestoneDAO;
import model.Inspection;
import model.Milestone;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class InspectorDashboard extends JFrame {

    private final User          user;
    private final MilestoneDAO  milestoneDAO  = new MilestoneDAO();
    private final InspectionDAOImpl inspectionDAO = new InspectionDAOImpl();
    private JPanel contentArea;

    private static final String[] NAV_ITEMS = {
        "Dashboard", "Inspect", "History"
    };

    public InspectorDashboard(User user) {
        this.user = user;

        setTitle("Nirikshan — Inspector");
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
            case "Dashboard" -> contentArea.add(buildDashboard(),  BorderLayout.CENTER);
            case "Inspect"   -> contentArea.add(buildInspect(),    BorderLayout.CENTER);
            case "History"   -> contentArea.add(buildHistory(),    BorderLayout.CENTER);
        }
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ══════════════════════════════════════════
    // SECTION 1 — Dashboard
    // ══════════════════════════════════════════
    private JPanel buildDashboard() {
        JPanel panel = basePanel();

        panel.add(pageTitle("Inspector Dashboard",
                "Welcome, " + user.getName()));
        panel.add(Box.createVerticalStrut(24));

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Count submitted milestones across all projects
        // (in real app you'd filter by assigned inspector)
        statsRow.add(statCard("Pending Review", "🔍", Theme.WARNING,
                "Milestones awaiting your inspection"));
        statsRow.add(statCard("Approved",        "✅", Theme.SUCCESS,
                "Milestones you approved"));
        statsRow.add(statCard("Rejected",        "❌", Theme.DANGER,
                "Milestones sent back for rework"));

        panel.add(statsRow);
        panel.add(Box.createVerticalStrut(30));

        // Info card
        RoundedPanel infoCard = new RoundedPanel(14, Theme.BG_CARD);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(new EmptyBorder(24, 24, 24, 24));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel infoTitle = new JLabel("🔍  Your Role in Nirikshan");
        infoTitle.setFont(Theme.FONT_LABEL);
        infoTitle.setForeground(Theme.TEXT_PRIMARY);

        String[] points = {
            "→  Review submitted milestones objectively",
            "→  Approve only if work quality meets standards",
            "→  Reject or request rework with clear remarks",
            "→  Your decision directly gates payment release"
        };

        infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(14));
        for (String pt : points) {
            JLabel l = new JLabel(pt);
            l.setFont(Theme.FONT_SUBTITLE);
            l.setForeground(Theme.TEXT_SECONDARY);
            infoCard.add(l);
            infoCard.add(Box.createVerticalStrut(6));
        }

        panel.add(infoCard);
        return panel;
    }

    // ══════════════════════════════════════════
    // SECTION 2 — Inspect Milestones
    // ══════════════════════════════════════════
    private JPanel buildInspect() {
        JPanel panel = basePanel();

        panel.add(pageTitle("Inspect Milestones",
                "Review submitted milestones and record your verdict"));
        panel.add(Box.createVerticalStrut(20));

        // Get all SUBMITTED milestones across all projects
        // We'll loop project IDs 1–100 for demo — in production
        // you'd have a direct query for submitted milestones
        JPanel cardsPanel = new JPanel();
        cardsPanel.setOpaque(false);
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));

        boolean anyFound = false;

        // Fetch submitted milestones using a direct DAO query
        List<Milestone> submitted = milestoneDAO.getMilestonesByStatus("SUBMITTED");

        for (Milestone m : submitted) {
            anyFound = true;
            cardsPanel.add(buildMilestoneInspectCard(m));
            cardsPanel.add(Box.createVerticalStrut(12));
        }

        if (!anyFound) {
            JLabel empty = new JLabel("✅  No milestones awaiting inspection.");
            empty.setFont(Theme.FONT_SUBTITLE);
            empty.setForeground(Theme.TEXT_SECONDARY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            cardsPanel.add(empty);
        }

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBackground(Theme.BG_MAIN);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(scroll);
        return panel;
    }

    private RoundedPanel buildMilestoneInspectCard(Milestone m) {
        RoundedPanel card = new RoundedPanel(14, Theme.BG_CARD);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Left — milestone info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel desc = new JLabel("Milestone #" + m.getMilestoneId()
                + "  —  " + m.getDescription());
        desc.setFont(Theme.FONT_LABEL);
        desc.setForeground(Theme.TEXT_PRIMARY);

        JLabel amount = new JLabel("₹ " + String.format("%,.2f", m.getBudget())
                + "   |   Project #" + m.getProjectId());
        amount.setFont(Theme.FONT_SUBTITLE);
        amount.setForeground(Theme.TEXT_SECONDARY);

        JLabel statusBadge = new JLabel("  SUBMITTED  ");
        statusBadge.setFont(Theme.FONT_SMALL);
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setBackground(Theme.WARNING);
        statusBadge.setOpaque(true);
        statusBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        info.add(desc);
        info.add(Box.createVerticalStrut(6));
        info.add(amount);
        info.add(Box.createVerticalStrut(6));
        info.add(statusBadge);

        // Right — action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButtonStyled approveBtn = Theme.successButton("✅ Approve");
        JButtonStyled rejectBtn  = Theme.dangerButton ("❌ Reject");

        approveBtn.setPreferredSize(new Dimension(120, 38));
        rejectBtn .setPreferredSize(new Dimension(120, 38));

        approveBtn.addActionListener(e ->
                showInspectionDialog(m, "APPROVED"));
        rejectBtn.addActionListener(e ->
                showInspectionDialog(m, "REJECTED"));

        actions.add(approveBtn);
        actions.add(rejectBtn);

        card.add(info,    BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private void showInspectionDialog(Milestone m, String result) {
        JDialog dialog = new JDialog(this, "Submit Inspection", true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);

        RoundedPanel card = new RoundedPanel(16, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        Color resultColor = result.equals("APPROVED") ? Theme.SUCCESS : Theme.DANGER;

        JLabel title = new JLabel(result.equals("APPROVED")
                ? "✅ Approve Milestone" : "❌ Reject Milestone");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(resultColor);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Milestone: " + m.getDescription());
        sub.setFont(Theme.FONT_SUBTITLE);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel remarkLabel = new JLabel("Remarks / Notes");
        remarkLabel.setFont(Theme.FONT_LABEL);
        remarkLabel.setForeground(Theme.TEXT_SECONDARY);
        remarkLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea remarksField = new JTextArea(3, 20);
        remarksField.setFont(Theme.FONT_INPUT);
        remarksField.setBackground(Theme.BG_INPUT);
        remarksField.setForeground(Theme.TEXT_PRIMARY);
        remarksField.setCaretColor(Theme.TEXT_PRIMARY);
        remarksField.setBorder(new EmptyBorder(10, 12, 10, 12));
        remarksField.setLineWrap(true);
        remarksField.setWrapStyleWord(true);
        remarksField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane remarksScroll = new JScrollPane(remarksField);
        remarksScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        remarksScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        remarksScroll.setBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1));

        JButtonStyled submitBtn = result.equals("APPROVED")
                ? Theme.successButton("Submit Approval")
                : Theme.dangerButton("Submit Rejection");
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        submitBtn.addActionListener(e -> {
            String remarks = remarksField.getText().trim();
            boolean ok = inspectionDAO.addInspection(
                    m.getMilestoneId(), user.getUserId(), result, remarks);

            if (ok) {
                // Update milestone status
                String newStatus = result.equals("APPROVED") ? "APPROVED" : "REJECTED";
                milestoneDAO.updateStatus(m.getMilestoneId(), newStatus);

                JOptionPane.showMessageDialog(dialog,
                        "Inspection submitted successfully!",
                        "✅ Done", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                showSection("Inspect");
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Failed to submit. Try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(20));
        card.add(remarkLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(remarksScroll);
        card.add(Box.createVerticalStrut(16));
        card.add(submitBtn);

        JPanel bg = new JPanel(new BorderLayout());
        bg.setBackground(Theme.BG_MAIN);
        bg.setBorder(new EmptyBorder(16, 16, 16, 16));
        bg.add(card, BorderLayout.CENTER);
        dialog.setContentPane(bg);
        dialog.setVisible(true);
    }

    // ══════════════════════════════════════════
    // SECTION 3 — Inspection History
    // ══════════════════════════════════════════
    private JPanel buildHistory() {
        JPanel panel = basePanel();

        panel.add(pageTitle("Inspection History",
                "All inspections submitted by you"));
        panel.add(Box.createVerticalStrut(20));

        List<Inspection> history =
                inspectionDAO.getInspectionsByInspector(user.getUserId());

        String[] cols = {"ID", "Milestone ID", "Result", "Remarks", "Date"};
        Object[][] data = new Object[history.size()][5];
        for (int i = 0; i < history.size(); i++) {
            Inspection ins = history.get(i);
            data[i] = new Object[]{
                ins.getInspectionId(),
                ins.getMilestoneId(),
                ins.getResult(),
                ins.getRemarks(),
                "Recorded"
            };
        }

        JTable table = createStyledTable(cols, data);

        // Color result column
        table.getColumnModel().getColumn(2)
                .setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                String v = val == null ? "" : val.toString();
                setForeground(switch (v) {
                    case "APPROVED"     -> Theme.SUCCESS;
                    case "REJECTED"     -> Theme.DANGER;
                    case "NEEDS_REWORK" -> Theme.WARNING;
                    default             -> Theme.TEXT_PRIMARY;
                });
                setBackground(sel ? Theme.PRIMARY_LIGHT :
                        (row % 2 == 0 ? Theme.BG_CARD : Theme.BG_INPUT));
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_CARD);
        scroll.getViewport().setBackground(Theme.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        panel.add(scroll);

        return panel;
    }

    // ══════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════
    private JPanel basePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_MAIN);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        return panel;
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

    private RoundedPanel statCard(String label, String icon,
                                   Color color, String sub) {
        RoundedPanel card = new RoundedPanel(14, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel iconLbl = new JLabel(icon + "  " + label);
        iconLbl.setFont(Theme.FONT_LABEL);
        iconLbl.setForeground(color);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(Theme.TEXT_SECONDARY);
        card.add(iconLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(subLbl);
        return card;
    }

    private JTable createStyledTable(String[] cols, Object[][] data) {
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
        return table;
    }
}