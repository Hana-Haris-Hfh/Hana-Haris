package com.studyspace.gui.components;

import com.studyspace.gui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Animated/transient toast banner displayed at the bottom right of the container.
 */
public class ToastNotification extends JPanel {

    private final JLabel label;
    private final Timer dismissTimer;

    public ToastNotification() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        label = new JLabel("");
        label.setFont(UITheme.FONT_BODY_BOLD);
        label.setForeground(Color.WHITE);
        add(label, BorderLayout.CENTER);

        setVisible(false);

        dismissTimer = new Timer(2500, e -> setVisible(false));
        dismissTimer.setRepeats(false);
    }

    public void showMessage(String message) {
        label.setText(message);
        setVisible(true);
        dismissTimer.restart();
    }

    @Override
    protected void paintComponent(Graphics g) {
        UITheme.applyAntiAliasing(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // Dark rounded pill background
        g2.setColor(new Color(41, 38, 62, 240));
        g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}
