package com.hackathon.manager.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import com.hackathon.manager.ui.Theme;

public class CustomButton extends JButton {
    public enum ButtonType {
        PRIMARY, SECONDARY, DANGER
    }

    private final ButtonType type;
    private boolean isHovered = false;
    private final int cornerRadius = 8;

    public CustomButton(String text, ButtonType type) {
        super(text);
        this.type = type;
        
        // Reset defaults
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(Theme.FONT_BODY_BOLD);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(120, 38));

        // Determine colors
        Color normalText, hoverBg, normalBg;
        if (type == ButtonType.PRIMARY) {
            normalBg = Theme.PRIMARY;
            hoverBg = Theme.PRIMARY_HOVER;
            normalText = Color.WHITE;
        } else if (type == ButtonType.DANGER) {
            normalBg = Theme.DANGER;
            hoverBg = new Color(220, 38, 38); // Darker red
            normalText = Color.WHITE;
        } else {
            // SECONDARY
            normalBg = Color.WHITE;
            hoverBg = Theme.PANEL_BG;
            normalText = Theme.TEXT_PRIMARY;
        }
        
        setForeground(normalText);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color bg;
        if (type == ButtonType.PRIMARY) {
            bg = isHovered ? Theme.PRIMARY_HOVER : Theme.PRIMARY;
        } else if (type == ButtonType.DANGER) {
            bg = isHovered ? new Color(220, 38, 38) : Theme.DANGER;
        } else {
            bg = isHovered ? Theme.PANEL_BG : Color.WHITE;
        }

        // Draw background
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

        // Draw border for secondary button
        if (type == ButtonType.SECONDARY) {
            g2.setColor(Theme.BORDER_COLOR);
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
