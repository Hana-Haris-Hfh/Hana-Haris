package com.studyspace.gui;

import com.studyspace.gui.components.*;
import com.studyspace.gui.dialog.TaskDialog;
import com.studyspace.gui.theme.UITheme;
import com.studyspace.model.Priority;
import com.studyspace.model.Task;
import com.studyspace.model.TaskStats;
import com.studyspace.model.TaskType;
import com.studyspace.service.StudyPlannerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main Desktop GUI application for StudySpace Smart Study Planner.
 */
public class StudyPlannerApp extends JFrame {

    private final StudyPlannerService service;

    // Navigation state
    private String currentView = "dashboard"; // "dashboard", "tasks", "today"
    private String currentStatusFilter = "all"; // "all", "pending", "completed"
    private String currentSearch = "";

    // Header labels
    private JLabel pageTitleLabel;
    private JLabel listTitleLabel;

    // Stat labels
    private JLabel totalCountLabel;
    private JLabel completedCountLabel;
    private JLabel pendingCountLabel;
    private JLabel progressPercentLabel;
    private ModernProgressBar progressBar;

    // Task list container & empty state
    private JPanel taskListPanel;
    private JPanel emptyStatePanel;
    private ModernTextField searchField;
    private JComboBox<String> statusFilterBox;

    // Quick planner fields
    private ModernTextField quickSubjectField;
    private ModernTextField quickGoalField;

    // Toast
    private ToastNotification toast;

    // Nav buttons
    private JButton navDashboardBtn;
    private JButton navTasksBtn;
    private JButton navTodayBtn;

    public StudyPlannerApp(StudyPlannerService service) {
        this.service = service;
        initWindow();
        initLayout();
        refreshAll();

        // Listen for data changes
        this.service.addChangeListener(this::refreshAll);
    }

    private void initWindow() {
        setTitle("StudySpace · Smart Study Planner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 800);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_MAIN);
    }

    private void initLayout() {
        // Main container with LayeredPane for Toast overlay
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        JPanel mainGrid = new JPanel(new BorderLayout());
        mainGrid.setBackground(UITheme.BG_MAIN);
        mainGrid.setBounds(0, 0, getWidth(), getHeight());

        // 1. Left Sidebar
        mainGrid.add(createSidebar(), BorderLayout.WEST);

        // 2. Center Content with scroll
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(UITheme.BG_MAIN);
        centerWrapper.setBorder(new EmptyBorder(26, 36, 26, 36));

        centerWrapper.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(UITheme.BG_MAIN);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));

        bodyPanel.add(createStatsRow());
        bodyPanel.add(Box.createVerticalStrut(20));
        bodyPanel.add(createContentSplit());

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UITheme.BG_MAIN);
        scrollPane.getViewport().setBackground(UITheme.BG_MAIN);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        mainGrid.add(centerWrapper, BorderLayout.CENTER);

        // Toast overlay
        toast = new ToastNotification();
        toast.setBounds(getWidth() - 340, getHeight() - 90, 300, 42);

        layeredPane.add(mainGrid, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(toast, JLayeredPane.POPUP_LAYER);

        // Resize listener to reposition Toast and mainGrid
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                mainGrid.setBounds(0, 0, getWidth() - 16, getHeight() - 39);
                toast.setBounds(getWidth() - 340, getHeight() - 100, 300, 42);
                layeredPane.revalidate();
            }
        });

        setContentPane(layeredPane);
    }

    // ==========================================
    // SIDEBAR
    // ==========================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.LINE_BORDER),
                new EmptyBorder(24, 16, 24, 16)
        ));

        // Brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        // Logo icon
        JPanel logo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                UITheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(164, 149, 244), getWidth(), getHeight(), UITheme.PURPLE_PRIMARY);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("S")) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString("S", x, y);
                g2.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(40, 40));
        brandPanel.add(logo);

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel appName = new JLabel("StudySpace");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appName.setForeground(UITheme.TEXT_MAIN);

        JLabel appTagline = new JLabel("SMART STUDY PLANNER");
        appTagline.setFont(new Font("Segoe UI", Font.BOLD, 9));
        appTagline.setForeground(new Color(153, 150, 173));

        brandText.add(appName);
        brandText.add(appTagline);
        brandPanel.add(brandText);

        // Navigation
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(30, 0, 0, 0));

        navDashboardBtn = createNavButton("▦   Dashboard", "dashboard");
        navTasksBtn = createNavButton("☷   All tasks", "tasks");
        navTodayBtn = createNavButton("◷   Today's plan", "today");

        navPanel.add(navDashboardBtn);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(navTasksBtn);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(navTodayBtn);

        JPanel topSidebar = new JPanel(new BorderLayout());
        topSidebar.setOpaque(false);
        topSidebar.add(brandPanel, BorderLayout.NORTH);
        topSidebar.add(navPanel, BorderLayout.CENTER);

        sidebar.add(topSidebar, BorderLayout.NORTH);

        // Bottom motivational card + footer
        JPanel bottomSidebar = new JPanel();
        bottomSidebar.setOpaque(false);
        bottomSidebar.setLayout(new BoxLayout(bottomSidebar, BoxLayout.Y_AXIS));

        CardPanel tipCard = new CardPanel(14, UITheme.PURPLE_LIGHT);
        tipCard.setLayout(new BoxLayout(tipCard, BoxLayout.Y_AXIS));
        tipCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel star = new JLabel("✦");
        star.setForeground(UITheme.PURPLE_PRIMARY);
        star.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel tipHeading = new JLabel("Small steps, big results.");
        tipHeading.setFont(UITheme.FONT_BODY_BOLD);
        tipHeading.setForeground(UITheme.TEXT_MAIN);

        JLabel tipBody = new JLabel("<html><p style='color:#77758c; font-size:11px; margin-top:4px;'>A little progress every day adds up to a lot.</p></html>");

        tipCard.add(star);
        tipCard.add(Box.createVerticalStrut(4));
        tipCard.add(tipHeading);
        tipCard.add(tipBody);

        bottomSidebar.add(tipCard);
        bottomSidebar.add(Box.createVerticalStrut(18));

        JLabel footer = new JLabel("Made for focused learning ✨", SwingConstants.CENTER);
        footer.setFont(UITheme.FONT_SMALL);
        footer.setForeground(UITheme.TEXT_LIGHT);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomSidebar.add(footer);

        sidebar.add(bottomSidebar, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String title, String viewName) {
        JButton btn = new JButton(title) {
            @Override
            protected void paintComponent(Graphics g) {
                UITheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                boolean isActive = currentView.equals(viewName);
                if (isActive) {
                    g2.setColor(UITheme.PURPLE_LIGHT);
                    g2.fillRoundRect(0, 0, w, h, 10, 10);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(248, 246, 255));
                    g2.fillRoundRect(0, 0, w, h, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(UITheme.TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(12, 14, 12, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        btn.addActionListener(e -> switchView(viewName));
        return btn;
    }

    private void switchView(String viewName) {
        this.currentView = viewName;
        navDashboardBtn.setForeground(viewName.equals("dashboard") ? UITheme.PURPLE_PRIMARY : UITheme.TEXT_MUTED);
        navTasksBtn.setForeground(viewName.equals("tasks") ? UITheme.PURPLE_PRIMARY : UITheme.TEXT_MUTED);
        navTodayBtn.setForeground(viewName.equals("today") ? UITheme.PURPLE_PRIMARY : UITheme.TEXT_MUTED);

        if ("today".equals(viewName)) {
            pageTitleLabel.setText("Today's plan ✦");
            listTitleLabel.setText("Tasks due today");
        } else if ("tasks".equals(viewName)) {
            pageTitleLabel.setText("All your tasks ✦");
            listTitleLabel.setText("All tasks");
        } else {
            pageTitleLabel.setText("Good day, student ✦");
            listTitleLabel.setText("Your tasks");
        }

        navDashboardBtn.repaint();
        navTasksBtn.repaint();
        navTodayBtn.repaint();

        refreshTasksList();
    }

    // ==========================================
    // TOP HEADER
    // ==========================================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 22, 0));

        JPanel leftHeader = new JPanel();
        leftHeader.setOpaque(false);
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d");
        JLabel dateLabel = new JLabel(LocalDate.now().format(dateFmt).toUpperCase());
        dateLabel.setFont(UITheme.FONT_EYEBROW);
        dateLabel.setForeground(UITheme.TEXT_MUTED);

        pageTitleLabel = new JLabel("Good day, student ✦");
        pageTitleLabel.setFont(UITheme.FONT_TITLE);
        pageTitleLabel.setForeground(UITheme.TEXT_MAIN);

        JLabel subLabel = new JLabel("Make today count. One task at a time.");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_MUTED);

        leftHeader.add(dateLabel);
        leftHeader.add(Box.createVerticalStrut(4));
        leftHeader.add(pageTitleLabel);
        leftHeader.add(Box.createVerticalStrut(4));
        leftHeader.add(subLabel);

        ModernButton addBtn = new ModernButton("＋  Add a task", ModernButton.Style.PRIMARY);
        addBtn.setFont(UITheme.FONT_BODY_BOLD);
        addBtn.setPreferredSize(new Dimension(145, 42));
        addBtn.addActionListener(e -> openTaskDialog(null));

        header.add(leftHeader, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        return header;
    }

    // ==========================================
    // STATS ROW (4 CARDS)
    // ==========================================
    private JPanel createStatsRow() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Card 1: Total
        totalCountLabel = new JLabel("0");
        statsPanel.add(createStatCard("Total tasks", "☷", totalCountLabel, "Tasks in your planner", UITheme.PURPLE_LIGHT, UITheme.PURPLE_PRIMARY));

        // Card 2: Completed
        completedCountLabel = new JLabel("0");
        statsPanel.add(createStatCard("Completed", "✓", completedCountLabel, "Look how far you've come", UITheme.GREEN_BG, UITheme.SUCCESS_GREEN));

        // Card 3: Pending
        pendingCountLabel = new JLabel("0");
        statsPanel.add(createStatCard("Pending", "◷", pendingCountLabel, "Ready for your attention", UITheme.ORANGE_BG, UITheme.ORANGE_TEXT));

        // Card 4: Progress
        progressPercentLabel = new JLabel("0%");
        progressBar = new ModernProgressBar();
        statsPanel.add(createProgressCard());

        return statsPanel;
    }

    private CardPanel createStatCard(String title, String symbol, JLabel countLabel, String subtitle, Color bubbleBg, Color bubbleColor) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Top line
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_BODY_BOLD);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel bubble = new JLabel(symbol, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                UITheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(bubbleBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubble.setPreferredSize(new Dimension(32, 32));
        bubble.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bubble.setForeground(bubbleColor);

        top.add(titleLbl, BorderLayout.WEST);
        top.add(bubble, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Center: number
        countLabel.setFont(UITheme.FONT_CARD_NUM);
        countLabel.setForeground(UITheme.TEXT_MAIN);
        countLabel.setBorder(new EmptyBorder(8, 0, 2, 0));
        card.add(countLabel, BorderLayout.CENTER);

        // Bottom: subtitle
        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.TEXT_LIGHT);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    private CardPanel createProgressCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLbl = new JLabel("Your progress");
        titleLbl.setFont(UITheme.FONT_BODY_BOLD);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel bubble = new JLabel("↗", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                UITheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.BLUE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubble.setPreferredSize(new Dimension(32, 32));
        bubble.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bubble.setForeground(UITheme.BLUE_TEXT);

        top.add(titleLbl, BorderLayout.WEST);
        top.add(bubble, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(14, 0, 8, 0));
        centerPanel.add(progressBar);
        card.add(centerPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        progressPercentLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        progressPercentLabel.setForeground(UITheme.TEXT_MAIN);

        JLabel completedLbl = new JLabel("completed");
        completedLbl.setFont(UITheme.FONT_SMALL);
        completedLbl.setForeground(UITheme.TEXT_LIGHT);

        bottom.add(progressPercentLabel, BorderLayout.WEST);
        bottom.add(completedLbl, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    // ==========================================
    // MAIN CONTENT SPLIT (LEFT TASKS, RIGHT ASIDE)
    // ==========================================
    private JPanel createContentSplit() {
        JPanel split = new JPanel(new BorderLayout(20, 0));
        split.setOpaque(false);

        // Left Panel: Tasks list
        split.add(createTasksPanel(), BorderLayout.CENTER);

        // Right Panel: Quick planner + Quote
        split.add(createRightAsidePanel(), BorderLayout.EAST);

        return split;
    }

    private JPanel createTasksPanel() {
        CardPanel panel = new CardPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(22, 22, 22, 22));

        // Header: Title & Filters
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("STAY ON TRACK");
        eyebrow.setFont(UITheme.FONT_EYEBROW);
        eyebrow.setForeground(UITheme.TEXT_MUTED);

        listTitleLabel = new JLabel("Your tasks");
        listTitleLabel.setFont(UITheme.FONT_HEADER);
        listTitleLabel.setForeground(UITheme.TEXT_MAIN);

        titlePanel.add(eyebrow);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(listTitleLabel);
        head.add(titlePanel, BorderLayout.WEST);

        // Search & Filter row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterRow.setOpaque(false);

        searchField = new ModernTextField("Search tasks...");
        searchField.setPreferredSize(new Dimension(160, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { onFilterChanged(); }
            @Override
            public void removeUpdate(DocumentEvent e) { onFilterChanged(); }
            @Override
            public void changedUpdate(DocumentEvent e) { onFilterChanged(); }
        });

        statusFilterBox = new JComboBox<>(new String[]{"All status", "Pending", "Completed"});
        statusFilterBox.setFont(UITheme.FONT_BODY);
        statusFilterBox.setBackground(Color.WHITE);
        statusFilterBox.setPreferredSize(new Dimension(115, 36));
        statusFilterBox.addActionListener(e -> onFilterChanged());

        filterRow.add(searchField);
        filterRow.add(statusFilterBox);
        head.add(filterRow, BorderLayout.EAST);

        panel.add(head, BorderLayout.NORTH);

        // Content: list or empty state
        JPanel listContainer = new JPanel(new BorderLayout());
        listContainer.setOpaque(false);

        taskListPanel = new JPanel();
        taskListPanel.setOpaque(false);
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));

        // Empty state panel
        emptyStatePanel = createEmptyStatePanel();

        listContainer.add(taskListPanel, BorderLayout.NORTH);
        listContainer.add(emptyStatePanel, BorderLayout.CENTER);

        panel.add(listContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEmptyStatePanel() {
        JPanel empty = new JPanel();
        empty.setOpaque(false);
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setBorder(new EmptyBorder(35, 10, 35, 10));

        JLabel icon = new JLabel("✎", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                UITheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UITheme.PURPLE_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setPreferredSize(new Dimension(50, 50));
        icon.setMaximumSize(new Dimension(50, 50));
        icon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        icon.setForeground(UITheme.PURPLE_PRIMARY);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("No tasks yet");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("<html><p align='center' style='color:#85839a; font-size:12px;'>Add your first study task and start building a good habit.</p></html>");
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        ModernButton createBtn = new ModernButton("＋  Create your first task", ModernButton.Style.SECONDARY);
        createBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        createBtn.addActionListener(e -> openTaskDialog(null));

        empty.add(icon);
        empty.add(Box.createVerticalStrut(12));
        empty.add(title);
        empty.add(Box.createVerticalStrut(6));
        empty.add(msg);
        empty.add(Box.createVerticalStrut(14));
        empty.add(createBtn);

        return empty;
    }

    // ==========================================
    // RIGHT ASIDE PANEL (QUICK PLANNER + QUOTE)
    // ==========================================
    private JPanel createRightAsidePanel() {
        JPanel aside = new JPanel();
        aside.setOpaque(false);
        aside.setLayout(new BoxLayout(aside, BoxLayout.Y_AXIS));
        aside.setPreferredSize(new Dimension(285, 0));

        // Quick Planner Card
        CardPanel quickCard = new CardPanel();
        quickCard.setLayout(new BoxLayout(quickCard, BoxLayout.Y_AXIS));
        quickCard.setBorder(new EmptyBorder(22, 20, 22, 20));

        JLabel eyebrow = new JLabel("QUICK PLANNER");
        eyebrow.setFont(UITheme.FONT_EYEBROW);
        eyebrow.setForeground(UITheme.TEXT_MUTED);

        JLabel heading = new JLabel("Plan a session");
        heading.setFont(UITheme.FONT_HEADER);
        heading.setForeground(UITheme.TEXT_MAIN);

        JLabel copy = new JLabel("Capture what you want to work on next.");
        copy.setFont(UITheme.FONT_SMALL);
        copy.setForeground(UITheme.TEXT_MUTED);

        quickSubjectField = new ModernTextField("e.g. Data Structures");
        quickSubjectField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        quickGoalField = new ModernTextField("e.g. Revise binary trees");
        quickGoalField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        ModernButton addPlanBtn = new ModernButton("Add to my plan  →", ModernButton.Style.PRIMARY);
        addPlanBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addPlanBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addPlanBtn.addActionListener(e -> onQuickAdd());

        quickCard.add(eyebrow);
        quickCard.add(Box.createVerticalStrut(4));
        quickCard.add(heading);
        quickCard.add(Box.createVerticalStrut(4));
        quickCard.add(copy);
        quickCard.add(Box.createVerticalStrut(14));

        JLabel lblSub = new JLabel("Subject");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        quickCard.add(lblSub);
        quickCard.add(Box.createVerticalStrut(4));
        quickCard.add(quickSubjectField);
        quickCard.add(Box.createVerticalStrut(10));

        JLabel lblGoal = new JLabel("Study goal");
        lblGoal.setFont(UITheme.FONT_SMALL);
        lblGoal.setForeground(UITheme.TEXT_MUTED);
        quickCard.add(lblGoal);
        quickCard.add(Box.createVerticalStrut(4));
        quickCard.add(quickGoalField);
        quickCard.add(Box.createVerticalStrut(14));
        quickCard.add(addPlanBtn);

        aside.add(quickCard);
        aside.add(Box.createVerticalStrut(18));

        // Quote Card (Gradient)
        JPanel quoteCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                UITheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(117, 100, 213), getWidth(), getHeight(), new Color(142, 123, 228));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        quoteCard.setOpaque(false);
        quoteCard.setLayout(new BoxLayout(quoteCard, BoxLayout.Y_AXIS));
        quoteCard.setBorder(new EmptyBorder(22, 20, 22, 20));

        JLabel qEyebrow = new JLabel("A LITTLE REMINDER");
        qEyebrow.setFont(UITheme.FONT_EYEBROW);
        qEyebrow.setForeground(new Color(222, 216, 255));

        JLabel quote = new JLabel("<html><b style='font-size:14px; line-height:1.5; color:white;'>“You don't have to be perfect. You just have to begin.”</b></html>");
        quote.setBorder(new EmptyBorder(12, 0, 16, 0));

        JLabel author = new JLabel("YOUR FUTURE SELF WILL THANK YOU");
        author.setFont(new Font("Segoe UI", Font.BOLD, 9));
        author.setForeground(new Color(225, 220, 255));

        quoteCard.add(qEyebrow);
        quoteCard.add(quote);
        quoteCard.add(author);

        aside.add(quoteCard);

        return aside;
    }

    // ==========================================
    // LOGIC & EVENTS
    // ==========================================
    private void onFilterChanged() {
        currentSearch = searchField.getText().trim();
        int sel = statusFilterBox.getSelectedIndex();
        if (sel == 1) currentStatusFilter = "pending";
        else if (sel == 2) currentStatusFilter = "completed";
        else currentStatusFilter = "all";

        refreshTasksList();
    }

    private void onQuickAdd() {
        String sub = quickSubjectField.getText().trim();
        String goal = quickGoalField.getText().trim();

        if (sub.isEmpty() || goal.isEmpty()) {
            toast.showMessage("Please fill in both subject and goal.");
            return;
        }

        service.createTask(sub, goal, LocalDate.now(), Priority.MEDIUM, TaskType.STUDY);
        quickSubjectField.setText("");
        quickGoalField.setText("");
        toast.showMessage("Study session added.");
    }

    private void openTaskDialog(Task existing) {
        TaskDialog dialog = new TaskDialog(this, existing);
        dialog.setVisible(true);

        Task result = dialog.getTaskResult();
        if (result != null) {
            if (existing != null) {
                service.updateTask(result.getId(), result.getSubject(), result.getDescription(), result.getDueDate(), result.getPriority(), result.getType());
                toast.showMessage("Task updated.");
            } else {
                service.createTask(result.getSubject(), result.getDescription(), result.getDueDate(), result.getPriority(), result.getType());
                toast.showMessage("Task added to your planner.");
            }
        }
    }

    private void onToggleDone(Task task) {
        boolean isDone = service.toggleTaskDone(task.getId());
        toast.showMessage(isDone ? "Great work! Task completed." : "Task moved to pending.");
    }

    private void onDeleteTask(Task task) {
        int opt = JOptionPane.showConfirmDialog(
                this,
                "Delete \"" + task.getDescription() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (opt == JOptionPane.YES_OPTION) {
            service.deleteTask(task.getId());
            toast.showMessage("Task deleted.");
        }
    }

    private void refreshAll() {
        refreshStats();
        refreshTasksList();
    }

    private void refreshStats() {
        TaskStats stats = service.getStats();
        totalCountLabel.setText(String.valueOf(stats.getTotal()));
        completedCountLabel.setText(String.valueOf(stats.getCompleted()));
        pendingCountLabel.setText(String.valueOf(stats.getPending()));
        progressPercentLabel.setText(stats.getPercentage() + "%");
        progressBar.setProgress(stats.getPercentage());
    }

    private void refreshTasksList() {
        taskListPanel.removeAll();

        List<Task> filtered = service.getFilteredTasks(currentView, currentStatusFilter, currentSearch);

        if (filtered.isEmpty()) {
            emptyStatePanel.setVisible(true);
            taskListPanel.setVisible(false);
        } else {
            emptyStatePanel.setVisible(false);
            taskListPanel.setVisible(true);

            for (Task t : filtered) {
                TaskItemPanel item = new TaskItemPanel(
                        t,
                        this::onToggleDone,
                        this::openTaskDialog,
                        this::onDeleteTask
                );
                taskListPanel.add(item);
            }
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }
}
