package com.studyspace.gui.theme;

import java.awt.*;

/**
 * Visual design system constants matching the Smart Study Planner aesthetic.
 */
public final class UITheme {

    private UITheme() {}

    // Color Palette
    public static final Color BG_MAIN = new Color(248, 248, 252);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color LINE_BORDER = new Color(238, 237, 245);
    public static final Color TEXT_MAIN = new Color(40, 38, 61);
    public static final Color TEXT_MUTED = new Color(133, 131, 154);
    public static final Color TEXT_LIGHT = new Color(170, 167, 185);

    // Brand & Accents
    public static final Color PURPLE_PRIMARY = new Color(118, 100, 216);
    public static final Color PURPLE_HOVER = new Color(93, 75, 192);
    public static final Color PURPLE_LIGHT = new Color(240, 237, 255);
    public static final Color ACCENT_GOLD = new Color(239, 187, 99);

    // Stat & Tag colors
    public static final Color SUCCESS_GREEN = new Color(85, 185, 148);
    public static final Color GREEN_BG = new Color(224, 245, 235);
    public static final Color ORANGE_TEXT = new Color(220, 148, 90);
    public static final Color ORANGE_BG = new Color(255, 240, 229);
    public static final Color BLUE_TEXT = new Color(88, 149, 209);
    public static final Color BLUE_BG = new Color(229, 242, 255);

    public static final Color PRIORITY_HIGH_TEXT = new Color(214, 110, 96);
    public static final Color PRIORITY_HIGH_BG = new Color(255, 240, 237);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_CARD_NUM = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_EYEBROW = new Font("Segoe UI", Font.BOLD, 10);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TAG = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Helper to enable high quality anti-aliasing for custom graphics.
     */
    public static void applyAntiAliasing(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }
}
