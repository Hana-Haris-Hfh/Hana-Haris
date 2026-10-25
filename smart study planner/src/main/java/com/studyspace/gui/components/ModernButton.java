package com.studyspace.gui.components;

import com.studyspace.gui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern rounded button with hover state transitions and customizable styles.
 */
public class ModernButton extends JButton {

    public enum Style {
        PRIMARY,
        SECONDARY,
        OUTLINE,
        ICON,
        DANGER
    }

    private final Style style;
    private boolean hovered = false;
    private int cornerRadius = 10;

    public ModernButton(String text) {
        this(text, Style.PRIMARY);
    }

    public ModernButton(String text, Style style) {
        super(text);
        this.style = style;
        initStyle();
    }

    private void initStyle() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(UITheme.FONT_BODY_BOLD);

        switch (style) {
            case PRIMARY:
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
                break;
            case SECONDARY:
                setForeground(UITheme.PURPLE_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
                break;
            case OUTLINE:
                setForeground(UITheme.TEXT_MAIN);
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                break;
            case ICON:
                setForeground(UITheme.TEXT_MUTED);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                cornerRadius = 8;
                break;
            case DANGER:
                setForeground(UITheme.PRIORITY_HIGH_TEXT);
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                break;
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        UITheme.applyAntiAliasing(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        Color bg = getBackground();
        Color border = null;

        switch (style) {
            case PRIMARY:
                bg = hovered ? UITheme.PURPLE_HOVER : UITheme.PURPLE_PRIMARY;
                break;
            case SECONDARY:
                bg = hovered ? new Color(230, 225, 255) : UITheme.PURPLE_LIGHT;
                break;
            case OUTLINE:
                bg = hovered ? new Color(245, 244, 250) : Color.WHITE;
                border = UITheme.LINE_BORDER;
                break;
            case ICON:
                bg = hovered ? new Color(235, 233, 245) : new Color(247, 246, 251);
                break;
            case DANGER:
                bg = hovered ? new Color(255, 225, 220) : UITheme.PRIORITY_HIGH_BG;
                break;
        }

        if (bg != null) {
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);
        }

        if (border != null) {
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
