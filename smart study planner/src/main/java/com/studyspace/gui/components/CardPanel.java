package com.studyspace.gui.components;

import com.studyspace.gui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * A sleek rounded card container with crisp borders and optional background color.
 */
public class CardPanel extends JPanel {

    private int cornerRadius = 16;
    private Color backgroundColor = UITheme.BG_CARD;
    private Color borderColor = UITheme.LINE_BORDER;
    private boolean drawBorder = true;

    public CardPanel() {
        this(16);
    }

    public CardPanel(int radius) {
        this.cornerRadius = radius;
        setOpaque(false);
    }

    public CardPanel(int radius, Color bg) {
        this.cornerRadius = radius;
        this.backgroundColor = bg;
        this.drawBorder = false;
        setOpaque(false);
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        UITheme.applyAntiAliasing(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int width = getWidth();
        int height = getHeight();

        // Draw card background
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

        // Draw card border
        if (drawBorder && borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
