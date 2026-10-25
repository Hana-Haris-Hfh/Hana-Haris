package com.studyspace.gui.dialog;

import com.studyspace.gui.components.ModernButton;
import com.studyspace.gui.components.ModernTextField;
import com.studyspace.gui.theme.UITheme;
import com.studyspace.model.Priority;
import com.studyspace.model.Task;
import com.studyspace.model.TaskType;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Clean modal dialog for creating or editing study tasks with form validation.
 */
public class TaskDialog extends JDialog {

    private final ModernTextField subjectField = new ModernTextField("e.g. Theory of Computation");
    private final ModernTextField descField = new ModernTextField("e.g. Practise pumping lemma questions");
    private final ModernTextField dueField = new ModernTextField(LocalDate.now().toString(), "YYYY-MM-DD");
    private final JComboBox<Priority> priorityCombo = new JComboBox<>(Priority.values());
    private final JComboBox<TaskType> typeCombo = new JComboBox<>(TaskType.values());
    private final JLabel errorLabel = new JLabel(" ");

    private Task taskResult = null;
    private final Task editingTask;

    public TaskDialog(Frame parent, Task existingTask) {
        super(parent, existingTask == null ? "Add a study task" : "Edit study task", true);
        this.editingTask = existingTask;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        initUI();

        if (existingTask != null) {
            subjectField.setText(existingTask.getSubject());
            descField.setText(existingTask.getDescription());
            dueField.setText(existingTask.getDueDate().toString());
            priorityCombo.setSelectedItem(existingTask.getPriority());
            typeCombo.setSelectedItem(existingTask.getType());
        } else {
            priorityCombo.setSelectedItem(Priority.MEDIUM);
            typeCombo.setSelectedItem(TaskType.STUDY);
        }

        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        // Main rounded card container
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                UITheme.applyAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 45));
                g2.fillRoundRect(2, 2, w - 4, h - 4, 18, 18);

                // Card body
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w - 5, h - 5, 18, 18);

                // Border
                g2.setColor(UITheme.LINE_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 5, h - 5, 18, 18);

                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setLayout(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        container.setPreferredSize(new Dimension(460, 490));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("MAKE A PLAN");
        eyebrow.setFont(UITheme.FONT_EYEBROW);
        eyebrow.setForeground(UITheme.TEXT_MUTED);
        titlePanel.add(eyebrow);

        titlePanel.add(Box.createVerticalStrut(4));

        JLabel title = new JLabel(editingTask == null ? "Add a study task" : "Edit study task");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_MAIN);
        titlePanel.add(title);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        ModernButton closeBtn = new ModernButton("✕", ModernButton.Style.ICON);
        closeBtn.setPreferredSize(new Dimension(30, 30));
        closeBtn.addActionListener(e -> dispose());
        headerPanel.add(closeBtn, BorderLayout.EAST);

        container.add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(18, 0, 10, 0));

        form.add(createFieldLabel("Subject"));
        form.add(Box.createVerticalStrut(4));
        form.add(subjectField);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Task description"));
        form.add(Box.createVerticalStrut(4));
        form.add(descField);
        form.add(Box.createVerticalStrut(10));

        // Two-column row for Due Date & Priority
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);

        JPanel colDate = new JPanel();
        colDate.setOpaque(false);
        colDate.setLayout(new BoxLayout(colDate, BoxLayout.Y_AXIS));
        colDate.add(createFieldLabel("Due date (YYYY-MM-DD)"));
        colDate.add(Box.createVerticalStrut(4));
        colDate.add(dueField);

        JPanel colPriority = new JPanel();
        colPriority.setOpaque(false);
        colPriority.setLayout(new BoxLayout(colPriority, BoxLayout.Y_AXIS));
        colPriority.add(createFieldLabel("Priority"));
        colPriority.add(Box.createVerticalStrut(4));
        styleComboBox(priorityCombo);
        colPriority.add(priorityCombo);

        row.add(colDate);
        row.add(colPriority);
        form.add(row);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Task type"));
        form.add(Box.createVerticalStrut(4));
        styleComboBox(typeCombo);
        form.add(typeCombo);
        form.add(Box.createVerticalStrut(8));

        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.PRIORITY_HIGH_TEXT);
        form.add(errorLabel);

        container.add(form, BorderLayout.CENTER);

        // Footer / Submit Button
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        ModernButton saveBtn = new ModernButton("Save task  ✓", ModernButton.Style.PRIMARY);
        saveBtn.setPreferredSize(new Dimension(120, 42));
        saveBtn.addActionListener(e -> onSave());

        footer.add(saveBtn, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);

        setContentPane(container);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(104, 102, 124));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(UITheme.FONT_BODY);
        box.setBackground(Color.WHITE);
        box.setForeground(UITheme.TEXT_MAIN);
        box.setFocusable(false);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, 38));
    }

    private void onSave() {
        String subject = subjectField.getText().trim();
        String desc = descField.getText().trim();
        String dueStr = dueField.getText().trim();

        if (subject.isEmpty()) {
            errorLabel.setText("Please enter a subject.");
            subjectField.requestFocus();
            return;
        }
        if (desc.isEmpty()) {
            errorLabel.setText("Please enter a task description.");
            descField.requestFocus();
            return;
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueStr);
        } catch (DateTimeParseException ex) {
            errorLabel.setText("Invalid date format. Please use YYYY-MM-DD.");
            dueField.requestFocus();
            return;
        }

        Priority priority = (Priority) priorityCombo.getSelectedItem();
        TaskType type = (TaskType) typeCombo.getSelectedItem();

        if (editingTask != null) {
            editingTask.setSubject(subject);
            editingTask.setDescription(desc);
            editingTask.setDueDate(dueDate);
            editingTask.setPriority(priority);
            editingTask.setType(type);
            taskResult = editingTask;
        } else {
            taskResult = new Task(System.currentTimeMillis(), subject, desc, dueDate, priority, type, false);
        }

        dispose();
    }

    public Task getTaskResult() {
        return taskResult;
    }
}
