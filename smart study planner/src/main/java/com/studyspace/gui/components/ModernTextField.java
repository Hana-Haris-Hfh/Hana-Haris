package com.studyspace.gui.components;

import com.studyspace.gui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Clean rounded text input field with placeholder hint and focus border.
 */
public class ModernTextField extends JTextField {

    private String placeholder = "";
    private boolean focused = false;
    private int cornerRadius = 9;

    public ModernTextField() {
        this("", "");
    }

    public ModernTextField(String placeholder) {
        this("", placeholder);
    }

    public ModernTextField(String text, String placeholder) {
        super(text);
        this.placeholder = placeholder != null ? placeholder : "";
        init();
    }

    private void init() {
        setOpaque(false);
        setFont(UITheme.FONT_BODY);
        setForeground(UITheme.TEXT_MAIN);
        setCaretColor(UITheme.PURPLE_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        UITheme.applyAntiAliasing(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // Fill background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

        // Draw border
        g2.setColor(focused ? UITheme.PURPLE_PRIMARY : UITheme.LINE_BORDER);
        g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);

        // Draw placeholder text if empty and not focused
        if (getText().isEmpty() && !placeholder.isEmpty()) {
            Graphics2D gPlaceholder = (Graphics2D) g.create();
            UITheme.applyAntiAliasing(gPlaceholder);
            gPlaceholder.setColor(UITheme.TEXT_MUTED);
            gPlaceholder.setFont(getFont());
            Insets insets = getInsets();
            FontMetrics fm = gPlaceholder.getFontMetrics();
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            gPlaceholder.drawString(placeholder, insets.left, y);
            gPlaceholder.dispose();
        }
    }
}
