package com.studyspace.gui.components;

import com.studyspace.gui.theme.UITheme;
import com.studyspace.model.Priority;
import com.studyspace.model.TaskType;

import javax.swing.*;
import java.awt.*;

/**
 * Modern pill badge for displaying tags, task types, and priority levels.
 */
public class TagBadge extends JComponent {

    private final String text;
    private final Color bgColor;
    private final Color textColor;

    public TagBadge(String text, Color bgColor, Color textColor) {
        this.text = text;
        this.bgColor = bgColor;
        this.textColor = textColor;
        setFont(UITheme.FONT_TAG);
    }

    public static TagBadge forType(TaskType type) {
        return new TagBadge(type.getDisplayName(), UITheme.PURPLE_LIGHT, UITheme.PURPLE_PRIMARY);
    }

    public static TagBadge forPriority(Priority priority) {
        switch (priority) {
            case HIGH:
                return new TagBadge(priority.getDisplayName(), UITheme.PRIORITY_HIGH_BG, UITheme.PRIORITY_HIGH_TEXT);
            case LOW:
                return new TagBadge(priority.getDisplayName(), UITheme.GREEN_BG, UITheme.SUCCESS_GREEN);
            case MEDIUM:
            default:
                return new TagBadge(priority.getDisplayName(), UITheme.PURPLE_LIGHT, UITheme.PURPLE_PRIMARY);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(text) + 14;
        int h = fm.getHeight() + 4;
        return new Dimension(w, Math.max(h, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        UITheme.applyAntiAliasing(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // Draw pill background
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, w, h, 6, 6);

        // Draw text
        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(text)) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);

        g2.dispose();
    }
}
