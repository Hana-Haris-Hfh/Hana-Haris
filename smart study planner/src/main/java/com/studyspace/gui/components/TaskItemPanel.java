package com.studyspace.gui.components;

import com.studyspace.gui.theme.UITheme;
import com.studyspace.model.Task;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Individual task row component with custom status toggle, strikethrough title,
 * metadata pills, and edit/delete actions.
 */
public class TaskItemPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final Task task;
    private final Consumer<Task> onToggle;
    private final Consumer<Task> onEdit;
    private final Consumer<Task> onDelete;

    public TaskItemPanel(Task task, Consumer<Task> onToggle, Consumer<Task> onEdit, Consumer<Task> onDelete) {
        this.task = task;
        this.onToggle = onToggle;
        this.onEdit = onEdit;
        this.onDelete = onDelete;

        setOpaque(false);
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.LINE_BORDER),
                BorderFactory.createEmptyBorder(12, 4, 12, 4)
        ));

        // 1. Checkbox toggle button
        JButton checkBtn = createCheckButton();
        JPanel checkWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        checkWrapper.setOpaque(false);
        checkWrapper.add(checkBtn);
        add(checkWrapper, BorderLayout.WEST);

        // 2. Middle info panel (Task name + meta badges)
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        // Task name/description
        JLabel nameLabel = new JLabel();
        if (task.isDone()) {
            nameLabel.setText("<html><strike style='color:#aaa8b6;'>" + escapeHtml(task.getDescription()) + "</strike></html>");
            nameLabel.setForeground(UITheme.TEXT_LIGHT);
        } else {
            nameLabel.setText(task.getDescription());
            nameLabel.setForeground(UITheme.TEXT_MAIN);
        }
        nameLabel.setFont(UITheme.FONT_BODY_BOLD);
        infoPanel.add(nameLabel);

        infoPanel.add(Box.createVerticalStrut(5));

        // Meta row: Subject • Due Date + Badges
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        metaPanel.setOpaque(false);

        JLabel subLabel = new JLabel(task.getSubject());
        subLabel.setFont(UITheme.FONT_SMALL);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        metaPanel.add(subLabel);

        JLabel dotLabel = new JLabel("•");
        dotLabel.setFont(UITheme.FONT_SMALL);
        dotLabel.setForeground(UITheme.TEXT_MUTED);
        metaPanel.add(dotLabel);

        String dueStr = task.getDueDate() != null ? task.getDueDate().format(DATE_FMT) : "No due date";
        JLabel dateLabel = new JLabel(dueStr);
        dateLabel.setFont(UITheme.FONT_SMALL);
        dateLabel.setForeground(task.isOverdue() ? UITheme.PRIORITY_HIGH_TEXT : UITheme.TEXT_MUTED);
        metaPanel.add(dateLabel);

        metaPanel.add(TagBadge.forType(task.getType()));
        metaPanel.add(TagBadge.forPriority(task.getPriority()));

        infoPanel.add(metaPanel);
        add(infoPanel, BorderLayout.CENTER);

        // 3. Actions: Edit and Delete buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actionsPanel.setOpaque(false);

        ModernButton editBtn = new ModernButton("✎", ModernButton.Style.ICON);
        editBtn.setToolTipText("Edit task");
        editBtn.setPreferredSize(new Dimension(28, 28));
        editBtn.addActionListener(e -> {
            if (onEdit != null) onEdit.accept(task);
        });

        ModernButton deleteBtn = new ModernButton("✕", ModernButton.Style.ICON);
        deleteBtn.setToolTipText("Delete task");
        deleteBtn.setPreferredSize(new Dimension(28, 28));
        deleteBtn.addActionListener(e -> {
            if (onDelete != null) onDelete.accept(task);
        });

        actionsPanel.add(editBtn);
        actionsPanel.add(deleteBtn);
        add(actionsPanel, BorderLayout.EAST);
    }

    private JButton createCheckButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                UITheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                if (task.isDone()) {
                    g2.setColor(UITheme.SUCCESS_GREEN);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 6, 6);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (w - fm.stringWidth("✓")) / 2;
                    int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString("✓", tx, ty);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 6, 6);
                    g2.setColor(new Color(215, 211, 232));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
                }
                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(20, 20));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(task.isDone() ? "Mark as pending" : "Mark as completed");
        btn.addActionListener(e -> {
            if (onToggle != null) onToggle.accept(task);
        });
        return btn;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
