package com.hackathon.manager.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;

public class Theme {
    // Colors
    public static final Color BG_COLOR = new Color(255, 255, 255);
    public static final Color PANEL_BG = new Color(249, 250, 251);     // F9FAFB
    public static final Color BORDER_COLOR = new Color(229, 231, 235); // E5E7EB
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);    // 111827
    public static final Color TEXT_SECONDARY = new Color(75, 85, 99);  // 4B5563
    public static final Color TEXT_LIGHT = new Color(156, 163, 175);   // 9CA3AF
    
    public static final Color PRIMARY = new Color(37, 99, 235);        // 2563EB (Royal Blue)
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color PRIMARY_LIGHT = new Color(239, 246, 255); // EFF6FF
    
    public static final Color ACCENT = new Color(99, 102, 241);        // 6366F1 (Indigo)
    public static final Color SUCCESS = new Color(16, 185, 129);       // 10B981 (Emerald)
    public static final Color SUCCESS_LIGHT = new Color(240, 253, 250);
    public static final Color WARNING = new Color(245, 158, 11);       // F59E0B (Amber)
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);
    public static final Color DANGER = new Color(239, 68, 68);         // EF4444 (Red)
    public static final Color DANGER_LIGHT = new Color(254, 242, 242);
    
    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);

    // Vector Icon Enum
    public enum IconType {
        DASHBOARD, TEAM, PARTICIPANT, JUDGE, PROJECT, ASSIGNMENT, EVALUATION, LEADERBOARD, CERTIFICATE, REPORT, LOGOUT
    }

    // Returns a custom vector-drawn icon
    public static Icon getIcon(IconType type, int size, Color color) {
        return new VectorIcon(type, size, color);
    }

    private static class VectorIcon implements Icon {
        private final IconType type;
        private final int size;
        private final Color color;

        public VectorIcon(IconType type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() { return size; }

        @Override
        public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Coordinates scaling factor
            double scale = size / 24.0;
            g2.translate(x, y);
            g2.scale(scale, scale);

            switch (type) {
                case DASHBOARD:
                    // 4 grid squares
                    g2.drawRect(3, 3, 7, 7);
                    g2.drawRect(14, 3, 7, 7);
                    g2.drawRect(3, 14, 7, 7);
                    g2.drawRect(14, 14, 7, 7);
                    break;
                case TEAM:
                    // User outline 1 (Left/Back)
                    g2.drawOval(5, 5, 6, 6); // head
                    Path2D body1 = new Path2D.Double();
                    body1.moveTo(2, 17);
                    body1.curveTo(2, 14, 5, 13, 8, 13);
                    body1.curveTo(11, 13, 14, 14, 14, 17);
                    g2.draw(body1);

                    // User outline 2 (Right/Front overlapping slightly)
                    g2.drawOval(13, 8, 6, 6); // head
                    Path2D body2 = new Path2D.Double();
                    body2.moveTo(10, 20);
                    body2.curveTo(10, 17, 13, 16, 16, 16);
                    body2.curveTo(19, 16, 22, 17, 22, 20);
                    g2.draw(body2);
                    break;
                case PARTICIPANT:
                    // Single user silhouette
                    g2.drawOval(8, 4, 8, 8); // Head
                    Path2D body = new Path2D.Double();
                    body.moveTo(4, 20);
                    body.curveTo(4, 15, 8, 14, 12, 14);
                    body.curveTo(16, 14, 20, 15, 20, 20);
                    g2.draw(body);
                    break;
                case JUDGE:
                    // Gavel / Star/Tie
                    // Gavel: drawing handle and head
                    g2.drawLine(6, 18, 18, 6); // handle
                    g2.drawLine(14, 2, 22, 10); // hammer head top
                    g2.drawLine(12, 4, 20, 12); // hammer head bottom
                    g2.drawLine(14, 2, 12, 4); // left side
                    g2.drawLine(22, 10, 20, 12); // right side
                    g2.drawLine(4, 20, 12, 20); // base stand
                    break;
                case PROJECT:
                    // Folder icon
                    Path2D folder = new Path2D.Double();
                    folder.moveTo(3, 19);
                    folder.lineTo(3, 5);
                    folder.curveTo(3, 4, 4, 3, 5, 3);
                    folder.lineTo(10, 3);
                    folder.lineTo(12, 6);
                    folder.lineTo(19, 6);
                    folder.curveTo(20, 6, 21, 7, 21, 8);
                    folder.lineTo(21, 19);
                    folder.curveTo(21, 20, 20, 21, 19, 21);
                    folder.lineTo(5, 21);
                    folder.curveTo(4, 21, 3, 20, 3, 19);
                    g2.draw(folder);
                    break;
                case ASSIGNMENT:
                    // Chain Link / User Assign
                    g2.drawOval(7, 3, 4, 4); // User head
                    Path2D userBody = new Path2D.Double();
                    userBody.moveTo(4, 13);
                    userBody.curveTo(4, 10, 6, 9, 9, 9);
                    userBody.curveTo(12, 9, 14, 10, 14, 13);
                    g2.draw(userBody);

                    // Document outline on the right with arrow pointing to it
                    g2.drawRect(14, 10, 7, 9);
                    g2.drawLine(14, 13, 17, 13);
                    // Connection line/arrow
                    g2.drawLine(9, 16, 12, 16);
                    g2.drawLine(12, 16, 11, 14);
                    g2.drawLine(12, 16, 11, 18);
                    break;
                case EVALUATION:
                    // Clipboard with checkmarks
                    g2.drawRect(5, 5, 14, 16); // clipboard outline
                    g2.drawRect(10, 3, 4, 3); // clip
                    // Checkmark
                    Path2D check = new Path2D.Double();
                    check.moveTo(9, 12);
                    check.lineTo(11, 14);
                    check.lineTo(15, 10);
                    g2.draw(check);
                    // Simple lines
                    g2.drawLine(8, 17, 16, 17);
                    break;
                case LEADERBOARD:
                    // 3-Bar Chart / Podium
                    g2.drawRect(4, 12, 4, 9); // Left bar (3rd)
                    g2.drawRect(10, 6, 4, 15); // Middle bar (1st)
                    g2.drawRect(16, 9, 4, 12); // Right bar (2nd)
                    // Draw a little crown on the middle bar
                    g2.drawLine(10, 3, 11, 4);
                    g2.drawLine(11, 4, 12, 3);
                    g2.drawLine(12, 3, 13, 4);
                    g2.drawLine(13, 4, 14, 3);
                    break;
                case CERTIFICATE:
                    // Diploma Scroll or Star Certificate
                    g2.drawRect(3, 4, 18, 14); // Certificate border
                    g2.drawOval(10, 9, 4, 4); // Seal circle
                    // Ribbon lines dangling from seal
                    g2.drawLine(11, 13, 9, 17);
                    g2.drawLine(13, 13, 15, 17);
                    // Corner lines
                    g2.drawLine(5, 6, 8, 6);
                    g2.drawLine(5, 6, 5, 9);
                    g2.drawLine(19, 6, 16, 6);
                    g2.drawLine(19, 6, 19, 9);
                    break;
                case REPORT:
                    // Graph/Pie Outline or Page with Line Charts
                    g2.drawRect(4, 3, 16, 18);
                    g2.drawLine(7, 7, 17, 7);
                    g2.drawLine(7, 11, 17, 11);
                    // Little chart drawing inside
                    Path2D reportLine = new Path2D.Double();
                    reportLine.moveTo(7, 18);
                    reportLine.lineTo(10, 15);
                    reportLine.lineTo(13, 16);
                    reportLine.lineTo(17, 13);
                    g2.draw(reportLine);
                    break;
                case LOGOUT:
                    // Sign out (Door + Arrow)
                    g2.drawRect(3, 3, 11, 18); // Door/Frame
                    // Arrow pointing right out of door
                    g2.drawLine(9, 12, 21, 12);
                    g2.drawLine(17, 8, 21, 12);
                    g2.drawLine(17, 16, 21, 12);
                    break;
            }

            g2.dispose();
        }
    }
}
