package com.hackathon.manager.ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JPasswordField;
import com.hackathon.manager.ui.Theme;

public class CustomPasswordField extends JPasswordField {
    private final String placeholder;
    private boolean isFocused = false;
    private final int cornerRadius = 8;

    public CustomPasswordField(String placeholder) {
        this.placeholder = placeholder;
        
        setBackground(Color.WHITE);
        setForeground(Theme.TEXT_PRIMARY);
        setCaretColor(Theme.PRIMARY);
        setFont(Theme.FONT_BODY);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Paint placeholder if password is empty
        if (getPassword().length == 0 && placeholder != null && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.TEXT_LIGHT);
            g2.setFont(Theme.FONT_BODY);
            Insets insets = getInsets();
            g2.drawString(placeholder, insets.left, g.getFontMetrics(Theme.FONT_BODY).getAscent() + insets.top);
            g2.dispose();
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();

        if (isFocused) {
            g2.setColor(Theme.PRIMARY);
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
            g2.setColor(new Color(37, 99, 235, 30));
            g2.drawRoundRect(1, 1, w - 3, h - 3, cornerRadius, cornerRadius);
        } else {
            g2.setColor(Theme.BORDER_COLOR);
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
    }
}
