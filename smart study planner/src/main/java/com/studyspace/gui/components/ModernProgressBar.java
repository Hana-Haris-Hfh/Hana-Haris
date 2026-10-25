package com.studyspace.gui.components;

import com.studyspace.gui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Modern rounded progress bar with a purple gradient fill.
 */
public class ModernProgressBar extends JComponent {

    private int progress = 0; // 0 to 100
    private static final Color TRACK_COLOR = new Color(240, 238, 248);
    private static final Color GRADIENT_START = new Color(169, 155, 240);
    private static final Color GRADIENT_END = new Color(118, 100, 216);

    public ModernProgressBar() {
        setPreferredSize(new Dimension(140, 8));
        setMinimumSize(new Dimension(50, 8));
    }

    public void setProgress(int value) {
        this.progress = Math.max(0, Math.min(100, value));
        repaint();
    }

    public int getProgress() {
        return progress;
    }

    @Override
    protected void paintComponent(Graphics g) {
        UITheme.applyAntiAliasing(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();
        int radius = h;

        // Draw background track
        g2.setColor(TRACK_COLOR);
        g2.fillRoundRect(0, 0, w, h, radius, radius);

        // Draw filled progress
        if (progress > 0) {
            int fillW = (int) (w * (progress / 100.0));
            if (fillW > 0) {
                GradientPaint gradient = new GradientPaint(0, 0, GRADIENT_START, fillW, 0, GRADIENT_END);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, Math.max(fillW, radius), h, radius, radius);
            }
        }

        g2.dispose();
    }
}
