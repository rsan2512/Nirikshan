package ui;

import dao.*;
import model.*;
import service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame {

    private final User           user;
    private final ProjectService projectService = new ProjectService();
    private final UserDAO        userDAO        = new UserDAO();
    private final PaymentService paymentService = new PaymentService();

    private JPanel contentArea;

    private static final String[] NAV_ITEMS = {
        "Dashboard", "Projects", "Milestones", "Payments", "Reports"
    };

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
    public AdminDashboard(User user) {
        this.user = user;

        setTitle("NIRIKSHAN — Admin Dashboard");
        setSize(1200, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Theme.BG_MAIN);

        // ── Root layout ──
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_MAIN);
        setContentPane(root);

        // ── Sidebar NavBar ──
        NavBar navBar = new NavBar(user, NAV_ITEMS, "Dashboard", section -> {
            showSection(section);
        });
        root.add(navBar, BorderLayout.WEST);

        // ── Content Area ──
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Theme.BG_MAIN);
        root.add(contentArea, BorderLayout.CENTER);

        // Show default section
        showSection("Dashboard");
        setVisible(true);
    }

    // ── Route sections ──
   private void showSection(String section) {
    contentArea.removeAll();
    try {
        switch (section) {
            case "Dashboard"  -> contentArea.add(buildDashboard(),  BorderLayout.CENTER);
            case "Projects"   -> contentArea.add(buildProjects(),   BorderLayout.CENTER);
            case "Milestones" -> contentArea.add(buildMilestones(), BorderLayout.CENTER);
            case "Payments"   -> contentArea.add(buildPayments(),   BorderLayout.CENTER);
            case "Reports"    -> contentArea.add(buildReports(),    BorderLayout.CENTER);
        }
    } catch (Exception e) {
        // Show what error is happening
        System.out.println("Error in section [" + section + "]: " + e.getMessage());
        e.printStackTrace();

        // Show error card in UI too
        RoundedPanel errCard = new RoundedPanel(14, Theme.BG_CARD);
        errCard.setLayout(new BoxLayout(errCard, BoxLayout.Y_AXIS));
        errCard.setBorder(new javax.swing.border.EmptyBorder(30, 30, 30, 30));
        JLabel errLabel = new JLabel("Error loading: " + e.getMessage());
        errLabel.setFont(Theme.FONT_LABEL);
        errLabel.setForeground(Theme.DANGER);
        errCard.add(errLabel);
        contentArea.add(errCard, BorderLayout.CENTER);
    }
    contentArea.revalidate();
    contentArea.repaint();
}

    // ══════════════════════════════════════════
    // SECTION 1 — Dashboard (Stats Overview)
    // ══════════════════════════════════════════
    private JPanel buildDashboard() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_MAIN);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Page title
        panel.add(pageTitle("Dashboard", "Welcome back, " + user.getName()));
        panel.add(Box.createVerticalStrut(24));

        // Stats row
        List<Project> projects = projectService.getAllProjects();
        long active    = projects.stream().filter(p -> p.getStatus().equals("ACTIVE")).count();
        long completed = projects.stream().filter(p -> p.getStatus().equals("COMPLETED")).count();

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        statsRow.add(statCard("Total Projects",    String.valueOf(projects.size()), "📁", Theme.PRIMARY));
        statsRow.add(statCard("Active",            String.valueOf(active),          "🔄", Theme.WARNING));
        statsRow.add(statCard("Completed",         String.valueOf(completed),       "✅", Theme.SUCCESS));
        statsRow.add(statCard("Contractors",
            String.valueOf(userDAO.getAllContractors().size()),                      "👷", Theme.DANGER));
        panel.add(statsRow);
        panel.add(Box.createVerticalStrut(30));

        // Recent projects table
        panel.add(sectionLabel("Recent Projects"));
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildProjectTable(projects));

        return panel;
    }

    // ══════════════════════════════════════════
    // SECTION 2 — Projects
    // ══════════════════════════════════════════
    private JPanel buildProjects() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_MAIN);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header row with Add button
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        headerRow.add(pageTitle("Projects", "Manage all government projects"), BorderLayout.WEST);

        JButtonStyled addBtn = Theme.primaryButton("+ New Project");
        addBtn.setPreferredSize(new Dimension(150, 40));
        addBtn.addActionListener(e -> showAddProjectDialog());
        headerRow.add(addBtn, BorderLayout.EAST);

        panel.add(headerRow);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buildProjectTable(projectService.getAllProjects()));

        return panel;
    }

    // ══════════════════════════════════════════
    // SECTION 3 — Milestones
    // ══════════════════════════════════════════
    private JPanel buildMilestones() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_MAIN);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        panel.add(pageTitle("Milestones", "Add and track project milestones"));
        panel.add(Box.createVerticalStrut(20));

        // Project selector
        JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        selectorRow.setOpaque(false);
        selectorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel selectLabel = new JLabel("Select Project:  ");
        selectLabel.setFont(Theme.FONT_LABEL);
        selectLabel.setForeground(Theme.TEXT_SECONDARY);

        List<Project> projects = projectService.getAllProjects();
        // NEW — one line
        JComboBox<Project> projectBox = styledComboBox(projects.toArray(new Project[0]));

        JButtonStyled addMilestoneBtn = Theme.primaryButton("+ Add Milestone");
        addMilestoneBtn.setPreferredSize(new Dimension(160, 38));

        // Milestone table panel (refreshable)
        JPanel tableHolder = new JPanel(new BorderLayout());
        tableHolder.setOpaque(false);

        Runnable refreshTable = () -> {
            tableHolder.removeAll();
            Project selected = (Project) projectBox.getSelectedItem();
            if (selected != null) {
                tableHolder.add(buildMilestoneTable(selected.getProjectId()),
                                BorderLayout.CENTER);
            }
            tableHolder.revalidate();
            tableHolder.repaint();
        };

        projectBox.addActionListener(e -> refreshTable.run());
        addMilestoneBtn.addActionListener(e -> {
            Project selected = (Project) projectBox.getSelectedItem();
            if (selected != null) {
                showAddMilestoneDialog(selected.getProjectId(), refreshTable);
            }
        });

        selectorRow.add(selectLabel);
        selectorRow.add(projectBox);
        selectorRow.add(Box.createHorizontalStrut(12));
        selectorRow.add(addMilestoneBtn);

        panel.add(selectorRow);
        panel.add(Box.createVerticalStrut(20));
        panel.add(tableHolder);

        // Load initial table
        if (!projects.isEmpty()) {
            tableHolder.add(buildMilestoneTable(projects.get(0).getProjectId()),
                            BorderLayout.CENTER);
        }

        return panel;
    }

    // ══════════════════════════════════════════
    // SECTION 4 — Payments
    // ══════════════════════════════════════════
    private JPanel buildPayments() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_MAIN);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        panel.add(pageTitle("Payments", "Review and release milestone payments"));
        panel.add(Box.createVerticalStrut(20));

        // Project selector
        JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        selectorRow.setOpaque(false);
        selectorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel selectLabel = new JLabel("Select Project:  ");
        selectLabel.setFont(Theme.FONT_LABEL);
        selectLabel.setForeground(Theme.TEXT_SECONDARY);

        List<Project> projects = projectService.getAllProjects();
        JComboBox<Project> projectBox = new JComboBox<>(projects.toArray(new Project[0]));
        projectBox.setFont(Theme.FONT_INPUT);
        projectBox.setBackground(Theme.BG_INPUT);
        projectBox.setForeground(Theme.TEXT_PRIMARY);
        projectBox.setPreferredSize(new Dimension(280, 38));

        selectorRow.add(selectLabel);
        selectorRow.add(projectBox);

        JPanel tableHolder = new JPanel(new BorderLayout());
        tableHolder.setOpaque(false);

        Runnable refreshTable = () -> {
            tableHolder.removeAll();
            Project selected = (Project) projectBox.getSelectedItem();
            if (selected != null) {
                tableHolder.add(buildPaymentTable(selected.getProjectId()),
                                BorderLayout.CENTER);
            }
            tableHolder.revalidate();
            tableHolder.repaint();
        };

        projectBox.addActionListener(e -> refreshTable.run());
        panel.add(selectorRow);
        panel.add(Box.createVerticalStrut(20));
        panel.add(tableHolder);

        if (!projects.isEmpty()) {
            tableHolder.add(buildPaymentTable(projects.get(0).getProjectId()),
                            BorderLayout.CENTER);
        }

        return panel;
    }

    // ══════════════════════════════════════════
    // SECTION 5 — Reports
    // ══════════════════════════════════════════
    private JPanel buildReports() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG_MAIN);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        panel.add(pageTitle("Reports", "System audit and summary"));
        panel.add(Box.createVerticalStrut(24));

        List<Project> projects = projectService.getAllProjects();
        double totalBudget = projects.stream().mapToDouble(Project::getTotalBudget).sum();

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        statsRow.add(statCard("Total Budget",
            "₹" + String.format("%,.0f", totalBudget), "💰", Theme.SUCCESS));
        statsRow.add(statCard("Total Projects",
            String.valueOf(projects.size()), "📁", Theme.PRIMARY));
        statsRow.add(statCard("Inspectors",
            String.valueOf(userDAO.getAllInspectors().size()), "🔍", Theme.WARNING));

        panel.add(statsRow);
        panel.add(Box.createVerticalStrut(30));
        panel.add(sectionLabel("All Projects Summary"));
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildProjectTable(projects));

        return panel;
    }

    // ══════════════════════════════════════════
    // TABLE BUILDERS
    // ══════════════════════════════════════════
    private JScrollPane buildProjectTable(List<Project> projects) {
        String[] cols = {"ID", "Project Name", "Location", "Budget (₹)", "Status"};
        Object[][] data = new Object[projects.size()][5];
        for (int i = 0; i < projects.size(); i++) {
            Project p = projects.get(i);
            data[i] = new Object[]{
                p.getProjectId(),
                p.getName(),
                p.getLocation(),
                String.format("%,.2f", p.getTotalBudget()),
                p.getStatus()
            };
        }
        return styledTable(cols, data);
    }

    private JScrollPane buildMilestoneTable(int projectId) {
        List<Milestone> milestones = projectService.getMilestones(projectId);
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
        return styledTable(cols, data);
    }

    private JScrollPane buildPaymentTable(int projectId) {
        List<Milestone> milestones = projectService.getMilestones(projectId);
        String[] cols = {"Milestone ID", "Description", "Amount (₹)", "Status", "Action"};
        Object[][] data = new Object[milestones.size()][5];
        for (int i = 0; i < milestones.size(); i++) {
            Milestone m = milestones.get(i);
            String eligibility = paymentService.checkEligibility(m.getMilestoneId());
            data[i] = new Object[]{
                m.getMilestoneId(),
                m.getDescription(),
                String.format("%,.2f", m.getBudget()),
                m.getStatus(),
                eligibility
            };
        }

        JTable table = createStyledJTable(cols, data);

        // Release Payment button column
        table.getColumn("Action").setCellRenderer((t, value, isSelected,
                                                    hasFocus, row, col) -> {
            JButtonStyled btn = Theme.successButton("Release");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            return btn;
        });

        table.getColumn("Action").setCellEditor(
            new DefaultCellEditor(new JCheckBox()) {
                @Override
                public boolean stopCellEditing() { return super.stopCellEditing(); }

                @Override
                public Component getTableCellEditorComponent(JTable table,
                        Object value, boolean isSelected, int row, int col) {
                    int milestoneId = (int) table.getValueAt(row, 0);
                    JButtonStyled btn = Theme.successButton("Release");
                    btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    btn.addActionListener(e -> {
                        String result = paymentService.releasePayment(milestoneId);
                        String[] parts = result.split("\\|");
                        String status  = parts[0];
                        String message = parts[1];
                        if (status.equals("RELEASED")) {
                            JOptionPane.showMessageDialog(AdminDashboard.this,
                                message, "✅ Payment Released",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(AdminDashboard.this,
                                message, "⚠ Payment On Hold",
                                JOptionPane.WARNING_MESSAGE);
                        }
                        showSection("Payments");
                    });
                    return btn;
                }
            }
        );

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_CARD);
        scroll.getViewport().setBackground(Theme.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return scroll;
    }

    // ══════════════════════════════════════════
    // DIALOGS
    // ══════════════════════════════════════════
    private void showAddProjectDialog() {
        JDialog dialog = new JDialog(this, "New Project", true);
        dialog.setSize(460, 380);
        dialog.setLocationRelativeTo(this);

        RoundedPanel card = new RoundedPanel(16, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Create New Project");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField     = styledField("Project Name");
        JTextField locationField = styledField("Location");
        JTextField budgetField   = styledField("Total Budget (₹)");

        List<User> contractors = userDAO.getAllContractors();
        JComboBox<User> contractorBox =
            new JComboBox<>(contractors.toArray(new User[0]));
        contractorBox.setFont(Theme.FONT_INPUT);
        contractorBox.setBackground(Theme.BG_INPUT);
        contractorBox.setForeground(Theme.TEXT_PRIMARY);
        contractorBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        contractorBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButtonStyled saveBtn = Theme.primaryButton("Create Project");
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            try {
                String name     = nameField.getText().trim();
                String location = locationField.getText().trim();
                double budget   = Double.parseDouble(budgetField.getText().trim());
                User contractor = (User) contractorBox.getSelectedItem();

                if (name.isEmpty() || location.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Please fill all fields.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean ok = projectService.createProject(name, location,
                                contractor.getUserId(), budget);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog,
                        "Project created successfully!", "✅ Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    showSection("Projects");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Enter a valid budget amount.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(title);
        card.add(Box.createVerticalStrut(20));
        card.add(formLabel("Project Name"));
        card.add(Box.createVerticalStrut(6));
        card.add(nameField);
        card.add(Box.createVerticalStrut(12));
        card.add(formLabel("Location"));
        card.add(Box.createVerticalStrut(6));
        card.add(locationField);
        card.add(Box.createVerticalStrut(12));
        card.add(formLabel("Total Budget (₹)"));
        card.add(Box.createVerticalStrut(6));
        card.add(budgetField);
        card.add(Box.createVerticalStrut(12));
        card.add(formLabel("Assign Contractor"));
        card.add(Box.createVerticalStrut(6));
        card.add(contractorBox);
        card.add(Box.createVerticalStrut(20));
        card.add(saveBtn);

        JPanel bg = new JPanel(new BorderLayout());
        bg.setBackground(Theme.BG_MAIN);
        bg.setBorder(new EmptyBorder(16, 16, 16, 16));
        bg.add(card, BorderLayout.CENTER);
        dialog.setContentPane(bg);
        dialog.setVisible(true);
    }

    private void showAddMilestoneDialog(int projectId, Runnable onSuccess) {
        JDialog dialog = new JDialog(this, "New Milestone", true);
        dialog.setSize(420, 290);
        dialog.setLocationRelativeTo(this);

        RoundedPanel card = new RoundedPanel(16, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Add Milestone");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField descField   = styledField("Milestone description");
        JTextField amountField = styledField("Amount (₹)");

        JButtonStyled saveBtn = Theme.primaryButton("Add Milestone");
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            try {
                String desc   = descField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());

                if (desc.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Please enter a description.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean ok = projectService.addMilestoneWithPayment(
                                projectId, desc, amount);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog,
                        "Milestone added!", "✅ Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    onSuccess.run();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Enter a valid amount.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(title);
        card.add(Box.createVerticalStrut(20));
        card.add(formLabel("Description"));
        card.add(Box.createVerticalStrut(6));
        card.add(descField);
        card.add(Box.createVerticalStrut(12));
        card.add(formLabel("Amount (₹)"));
        card.add(Box.createVerticalStrut(6));
        card.add(amountField);
        card.add(Box.createVerticalStrut(20));
        card.add(saveBtn);

        JPanel bg = new JPanel(new BorderLayout());
        bg.setBackground(Theme.BG_MAIN);
        bg.setBorder(new EmptyBorder(16, 16, 16, 16));
        bg.add(card, BorderLayout.CENTER);
        dialog.setContentPane(bg);
        dialog.setVisible(true);
    }

    // ══════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════
    private JScrollPane styledTable(String[] cols, Object[][] data) {
        JTable table = createStyledJTable(cols, data);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.BG_CARD);
        scroll.getViewport().setBackground(Theme.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return scroll;
    }

    private JTable createStyledJTable(String[] cols, Object[][] data) {
        JTable table = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) {
                return getColumnName(c).equals("Action");
            }
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
        table.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        table.setSelectionBackground(Theme.PRIMARY_LIGHT);
        table.setSelectionForeground(Theme.PRIMARY_DARK);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBackground(sel ? Theme.PRIMARY_LIGHT :
                    (row % 2 == 0 ? Theme.BG_CARD : Theme.BG_INPUT));
                setForeground(sel ? Theme.PRIMARY_DARK : Theme.TEXT_PRIMARY);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });
        return table;
    }

    private RoundedPanel statCard(String label, String value,
                                   String icon, Color color) {
        RoundedPanel card = new RoundedPanel(14, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel iconLabel = new JLabel(icon + "  " + label);
        iconLabel.setFont(Theme.FONT_SMALL);
        iconLabel.setForeground(Theme.TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(color);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        return card;
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

        p.add(t);
        p.add(s);
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(Theme.FONT_INPUT);
        field.setBackground(Theme.BG_INPUT);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }
}