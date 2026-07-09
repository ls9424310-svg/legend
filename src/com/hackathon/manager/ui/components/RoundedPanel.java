package com.hackathon.manager.ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import com.hackathon.manager.ui.Theme;

public class RoundedPanel extends JPanel {
    private final int cornerRadius;
    private final Color bgColor;
    private final boolean drawBorder;
    private final Color borderColor;

    public RoundedPanel(int radius, Color bgColor, boolean drawBorder, Color borderColor) {
        this.cornerRadius = radius;
        this.bgColor = bgColor;
        this.drawBorder = drawBorder;
        this.borderColor = borderColor;
        setOpaque(false);
    }

    public RoundedPanel(int radius) {
        this(radius, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

        // Border
        if (drawBorder) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
    }
}
