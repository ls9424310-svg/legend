package com.hackathon.manager.ui.components;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import com.hackathon.manager.ui.Theme;

public class AnalyticsChart extends JComponent {
    public enum ChartType {
        DONUT, BAR, LINE
    }

    private final ChartType type;
    private final String title;
    private final List<String> labels = new ArrayList<>();
    private final List<Double> values = new ArrayList<>();

    // Curated color palette for chart segments
    private static final Color[] COLORS = {
        Theme.PRIMARY,
        Theme.ACCENT,
        Theme.SUCCESS,
        Theme.WARNING,
        Theme.DANGER,
        new Color(14, 116, 144),  // Cyan
        new Color(109, 40, 217),  // Purple
        new Color(219, 39, 119)   // Pink
    };

    public AnalyticsChart(ChartType type, String title) {
        this.type = type;
        this.title = title;
    }

    public void addData(String label, double value) {
        labels.add(label);
        values.add(value);
        repaint();
    }

    public void clearData() {
        labels.clear();
        values.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Draw title
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.setFont(Theme.FONT_BODY_BOLD);
        g2.drawString(title, 20, 25);

        if (values.isEmpty()) {
            g2.setFont(Theme.FONT_BODY);
            g2.setColor(Theme.TEXT_LIGHT);
            String emptyMsg = "No data available";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(emptyMsg, (w - fm.stringWidth(emptyMsg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        // Draw depending on type
        switch (type) {
            case DONUT:
                drawDonutChart(g2, w, h);
                break;
            case BAR:
                drawBarChart(g2, w, h);
                break;
            case LINE:
                drawLineChart(g2, w, h);
                break;
        }

        g2.dispose();
    }

    private void drawDonutChart(Graphics2D g2, int w, int h) {
        double total = 0;
        for (double v : values) total += v;

        int diameter = Math.min(w, h) - 90;
        int cx = (w - diameter) / 2 - 40; // Shift left for legend space
        int cy = (h - diameter) / 2 + 10;

        double startAngle = 90;
        for (int i = 0; i < values.size(); i++) {
            double angle = (values.get(i) / total) * 360;
            g2.setColor(COLORS[i % COLORS.length]);
            g2.fillArc(cx, cy, diameter, diameter, (int) Math.round(startAngle), (int) Math.round(-angle));
            startAngle -= angle;
        }

        // Cut out center to make it a donut
        g2.setColor(Color.WHITE);
        int innerDiameter = (int) (diameter * 0.55);
        int icx = cx + (diameter - innerDiameter) / 2;
        int icy = cy + (diameter - innerDiameter) / 2;
        g2.fillOval(icx, icy, innerDiameter, innerDiameter);

        // Draw Legends on the Right
        int legendX = cx + diameter + 30;
        int legendY = cy + 15;
        g2.setFont(Theme.FONT_SMALL);

        for (int i = 0; i < labels.size(); i++) {
            g2.setColor(COLORS[i % COLORS.length]);
            g2.fillRoundRect(legendX, legendY, 12, 12, 3, 3);
            
            g2.setColor(Theme.TEXT_SECONDARY);
            String percent = String.format(" (%.1f%%)", (values.get(i) / total) * 100);
            g2.drawString(labels.get(i) + percent, legendX + 20, legendY + 10);
            legendY += 22;
        }
    }

    private void drawBarChart(Graphics2D g2, int w, int h) {
        int paddingLeft = 45;
        int paddingBottom = 40;
        int paddingTop = 45;
        int paddingRight = 20;

        int chartWidth = w - paddingLeft - paddingRight;
        int chartHeight = h - paddingTop - paddingBottom;

        // Draw background horizontal grid lines
        g2.setStroke(new BasicStroke(1.0f));
        g2.setFont(Theme.FONT_SMALL);
        for (int i = 0; i <= 5; i++) {
            int gridY = paddingTop + chartHeight - (chartHeight * i / 5);
            g2.setColor(Theme.BORDER_COLOR);
            g2.drawLine(paddingLeft, gridY, paddingLeft + chartWidth, gridY);
            
            g2.setColor(Theme.TEXT_LIGHT);
            g2.drawString(String.valueOf(10 * i), 15, gridY + 4);
        }

        // Draw Bars
        int barCount = values.size();
        int spacing = 15;
        int totalSpacing = spacing * (barCount + 1);
        int barWidth = (chartWidth - totalSpacing) / barCount;

        double maxVal = 50.0; // Evaluation total score max is 50

        for (int i = 0; i < barCount; i++) {
            double val = values.get(i);
            int barHeight = (int) ((val / maxVal) * chartHeight);
            int barX = paddingLeft + spacing + i * (barWidth + spacing);
            int barY = paddingTop + chartHeight - barHeight;

            // Draw shadow/gradient bar
            GradientPaint gp = new GradientPaint(
                barX, barY, Theme.PRIMARY,
                barX, barY + barHeight, Theme.PRIMARY_LIGHT
            );
            g2.setPaint(gp);
            g2.fillRoundRect(barX, barY, barWidth, barHeight, 6, 6);

            // Draw Value on top of bar
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.setFont(Theme.FONT_SMALL_BOLD);
            String valStr = String.format("%.1f", val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, barX + (barWidth - fm.stringWidth(valStr)) / 2, barY - 6);

            // Draw X Axis label
            g2.setColor(Theme.TEXT_SECONDARY);
            g2.setFont(Theme.FONT_SMALL);
            String label = labels.get(i);
            // Truncate label if too long
            if (label.length() > 8) label = label.substring(0, 7) + "..";
            int labelWidth = fm.stringWidth(label);
            g2.drawString(label, barX + (barWidth - labelWidth) / 2, paddingTop + chartHeight + 20);
        }
    }

    private void drawLineChart(Graphics2D g2, int w, int h) {
        int paddingLeft = 45;
        int paddingBottom = 40;
        int paddingTop = 45;
        int paddingRight = 20;

        int chartWidth = w - paddingLeft - paddingRight;
        int chartHeight = h - paddingTop - paddingBottom;

        double maxVal = 0.0;
        for (double v : values) if (v > maxVal) maxVal = v;
        if (maxVal == 0) maxVal = 10.0;
        // round to nearest multiple of 10
        maxVal = Math.ceil(maxVal / 10.0) * 10.0;

        // Draw horizontal grid lines
        g2.setFont(Theme.FONT_SMALL);
        for (int i = 0; i <= 4; i++) {
            int gridY = paddingTop + chartHeight - (chartHeight * i / 4);
            g2.setColor(Theme.BORDER_COLOR);
            g2.drawLine(paddingLeft, gridY, paddingLeft + chartWidth, gridY);
            
            g2.setColor(Theme.TEXT_LIGHT);
            int valLabel = (int) (maxVal * i / 4);
            g2.drawString(String.valueOf(valLabel), 15, gridY + 4);
        }

        // Calculate points
        int pointsCount = values.size();
        int[] px = new int[pointsCount];
        int[] py = new int[pointsCount];

        for (int i = 0; i < pointsCount; i++) {
            px[i] = paddingLeft + (chartWidth * i / (pointsCount > 1 ? pointsCount - 1 : 1));
            py[i] = (int) (paddingTop + chartHeight - (values.get(i) / maxVal * chartHeight));
        }

        // Draw area gradient under the line
        if (pointsCount > 1) {
            Path2D area = new Path2D.Double();
            area.moveTo(px[0], paddingTop + chartHeight);
            for (int i = 0; i < pointsCount; i++) {
                area.lineTo(px[i], py[i]);
            }
            area.lineTo(px[pointsCount - 1], paddingTop + chartHeight);
            area.closePath();

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
            GradientPaint areaGlow = new GradientPaint(
                0, paddingTop, Theme.PRIMARY,
                0, paddingTop + chartHeight, Color.WHITE
            );
            g2.setPaint(areaGlow);
            g2.fill(area);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Draw line connection
        g2.setColor(Theme.PRIMARY);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < pointsCount - 1; i++) {
            g2.drawLine(px[i], py[i], px[i+1], py[i+1]);
        }

        // Draw node circles
        g2.setStroke(new BasicStroke(2.0f));
        for (int i = 0; i < pointsCount; i++) {
            g2.setColor(Color.WHITE);
            g2.fillOval(px[i] - 5, py[i] - 5, 10, 10);
            g2.setColor(Theme.PRIMARY);
            g2.drawOval(px[i] - 5, py[i] - 5, 10, 10);

            // Draw Value
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.setFont(Theme.FONT_SMALL_BOLD);
            String valStr = String.valueOf(values.get(i).intValue());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, px[i] - fm.stringWidth(valStr) / 2, py[i] - 10);

            // Draw X Axis label
            g2.setColor(Theme.TEXT_SECONDARY);
            g2.setFont(Theme.FONT_SMALL);
            String label = labels.get(i);
            int labelWidth = fm.stringWidth(label);
            g2.drawString(label, px[i] - labelWidth / 2, paddingTop + chartHeight + 20);
        }
    }
}
