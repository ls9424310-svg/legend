package com.hackathon.manager.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.hackathon.manager.ui.Theme;
import com.hackathon.manager.ui.components.CustomButton;
import com.hackathon.manager.ui.components.RoundedPanel;
import com.hackathon.manager.util.PDFReportGenerator;

public class ReportsPanel extends JPanel {
    private final boolean isAdmin;
    private final int judgeId;

    public ReportsPanel() {
        this(0); // Admin constructor has judgeId = 0
    }

    public ReportsPanel(int judgeId) {
        this.judgeId = judgeId;
        this.isAdmin = (judgeId == 0);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("PDF Reports Panel");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        setupUI();
    }

    private void setupUI() {
        JPanel centerGrid = new JPanel(new GridBagLayout());
        centerGrid.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;

        if (isAdmin) {
            // Admin gets 4 reports in a 2x2 grid layout
            gbc.gridx = 0; gbc.gridy = 0;
            centerGrid.add(createReportCard(
                "Teams & Members Report",
                "Contains details of all registered teams, college affiliations, leaders, and their respective team members.",
                "team_report.pdf", 1
            ), gbc);

            gbc.gridx = 1; gbc.gridy = 0;
            centerGrid.add(createReportCard(
                "Judge Evaluations Report",
                "Lists all judges, their assigned hackathon projects, and evaluation scores given so far.",
                "judge_report.pdf", 2
            ), gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            centerGrid.add(createReportCard(
                "Domain-Wise Projects Report",
                "Displays projects grouped by domain (AI/ML, Web Development, Android, IoT, Cyber Security).",
                "project_report.pdf", 3
            ), gbc);

            gbc.gridx = 1; gbc.gridy = 1;
            centerGrid.add(createReportCard(
                "Winner & Leaderboard Report",
                "Displays the final scoreboard ranking and details of top-scoring projects across categories.",
                "winner_report.pdf", 4
            ), gbc);
        } else {
            // Judge only gets 1 report card in center (their scores)
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            centerGrid.add(createReportCard(
                "My Evaluation Scores Report",
                "Contains list of all projects assigned to you, along with scores you have submitted.",
                "my_judge_report.pdf", 2
            ), gbc);
        }

        add(centerGrid, BorderLayout.CENTER);
    }

    private RoundedPanel createReportCard(String titleText, String descText, String defaultFilename, int reportType) {
        RoundedPanel card = new RoundedPanel(10, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Title
        JLabel title = new JLabel(titleText);
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(title, gbc);

        // Description
        JLabel desc = new JLabel("<html><p style='width: 200px;'>" + descText + "</p></html>");
        desc.setFont(Theme.FONT_BODY);
        desc.setForeground(Theme.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 0, 15, 0);
        card.add(desc, gbc);

        // Button
        CustomButton btnExport = new CustomButton("Export PDF", CustomButton.ButtonType.PRIMARY);
        btnExport.setPreferredSize(new Dimension(140, 36));
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(5, 0, 0, 0);
        card.add(btnExport, gbc);

        btnExport.addActionListener(e -> exportReport(defaultFilename, reportType));

        return card;
    }

    private void exportReport(String defaultFilename, int reportType) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        fileChooser.setSelectedFile(new File(defaultFilename));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String dest = fileToSave.getAbsolutePath();
            if (!dest.toLowerCase().endsWith(".pdf")) {
                dest += ".pdf";
            }

            try {
                switch (reportType) {
                    case 1:
                        PDFReportGenerator.generateTeamReport(dest);
                        break;
                    case 2:
                        PDFReportGenerator.generateJudgeReport(dest);
                        break;
                    case 3:
                        PDFReportGenerator.generateProjectReport(dest);
                        break;
                    case 4:
                        PDFReportGenerator.generateWinnerReport(dest);
                        break;
                }
                JOptionPane.showMessageDialog(this, "Report exported successfully!\nSaved to: " + dest, "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to export report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}
