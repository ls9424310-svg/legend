package com.hackathon.manager.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import com.hackathon.manager.db.DatabaseHelper;
import com.hackathon.manager.ui.Theme;
import com.hackathon.manager.ui.components.CustomButton;
import com.hackathon.manager.ui.components.CustomTable;
import com.hackathon.manager.ui.components.RoundedPanel;
import com.hackathon.manager.util.Recommender;

public class EvaluationPanel extends JPanel {
    private final String role;
    private final int judgeId;
    private final String judgeName;

    // UI elements for Judge
    private JComboBox<Recommender.Recommendation> projectCombo;
    private JSlider sInnovation, sTechnical, sUiux, sPresentation, sImpact;
    private JLabel lblTotalScore;
    private JTextArea txtProjDetails;
    private CustomButton btnSubmit;
    private JButton btnGithub, btnPpt;

    // UI elements for Admin
    private CustomTable table;
    private DefaultTableModel tableModel;

    private int currentTotalScore = 0;
    private String currentGithubUrl = "";
    private String currentPptUrl = "";

    public EvaluationPanel(String role, int judgeId, String judgeName) {
        this.role = role;
        this.judgeId = judgeId;
        this.judgeName = judgeName;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Judge".equals(role) ? "Project Evaluation Sheet" : "Evaluation Records");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        if ("Judge".equals(role)) {
            setupJudgeUI();
        } else {
            setupAdminUI();
        }
    }

    private void setupAdminUI() {
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);

        String[] columns = {"Eval ID", "Team Name", "Project Title", "Judge Name", "Innovation", "Tech", "UI/UX", "Pres.", "Impact", "Total / 50"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new CustomTable(tableModel);
        JScrollPane scroll = CustomTable.createScrollPane(table);
        tableContainer.add(scroll, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        loadAllEvaluations();
    }

    private void setupJudgeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(25, 0));
        mainPanel.setBackground(Color.WHITE);

        // 1. Left Details & Selector Column
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BorderLayout(0, 15));
        leftCol.setBackground(Color.WHITE);
        leftCol.setPreferredSize(new Dimension(360, 500));

        RoundedPanel selectorCard = new RoundedPanel(10, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
        selectorCard.setLayout(new GridBagLayout());
        selectorCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        selectorCard.setPreferredSize(new Dimension(360, 160));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.insets = new Insets(4, 0, 4, 0);

        JLabel lblSel = new JLabel("Select Assigned Project");
        lblSel.setFont(Theme.FONT_BODY_BOLD);
        lblSel.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 0;
        selectorCard.add(lblSel, gbc);

        projectCombo = new JComboBox<>();
        projectCombo.setFont(Theme.FONT_BODY);
        projectCombo.setBackground(Color.WHITE);
        projectCombo.setPreferredSize(new Dimension(310, 36));
        gbc.gridy = 1;
        selectorCard.add(projectCombo, gbc);

        // Links row
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        linksPanel.setBackground(Theme.PANEL_BG);
        btnGithub = new JButton("Open GitHub");
        btnGithub.setFont(Theme.FONT_SMALL_BOLD);
        btnPpt = new JButton("Open Slides (PPT)");
        btnPpt.setFont(Theme.FONT_SMALL_BOLD);
        linksPanel.add(btnGithub);
        linksPanel.add(btnPpt);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        selectorCard.add(linksPanel, gbc);

        leftCol.add(selectorCard, BorderLayout.NORTH);

        // Details text area card
        RoundedPanel detailsCard = new RoundedPanel(10, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
        detailsCard.setLayout(new BorderLayout());
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel lblDet = new JLabel("Project Information & Scope");
        lblDet.setFont(Theme.FONT_BODY_BOLD);
        lblDet.setForeground(Theme.TEXT_PRIMARY);
        lblDet.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        detailsCard.add(lblDet, BorderLayout.NORTH);

        txtProjDetails = new JTextArea();
        txtProjDetails.setEditable(false);
        txtProjDetails.setLineWrap(true);
        txtProjDetails.setWrapStyleWord(true);
        txtProjDetails.setFont(Theme.FONT_BODY);
        txtProjDetails.setBackground(Theme.PANEL_BG);
        detailsCard.add(new JScrollPane(txtProjDetails), BorderLayout.CENTER);

        leftCol.add(detailsCard, BorderLayout.CENTER);
        mainPanel.add(leftCol, BorderLayout.WEST);

        // 2. Right Score Sliders Column
        RoundedPanel slidersCard = new RoundedPanel(10, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
        slidersCard.setLayout(new GridBagLayout());
        slidersCard.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbcS = new GridBagConstraints();
        gbcS.fill = GridBagConstraints.HORIZONTAL;
        gbcS.gridx = 0;
        gbcS.weightx = 1.0;
        gbcS.insets = new Insets(4, 0, 4, 0);

        // Slider Labels and sliders
        sInnovation = addSlider(slidersCard, "Innovation & Novelty (0-10)", 0, gbcS);
        sTechnical = addSlider(slidersCard, "Technical Complexity (0-10)", 2, gbcS);
        sUiux = addSlider(slidersCard, "UI/UX & Design Quality (0-10)", 4, gbcS);
        sPresentation = addSlider(slidersCard, "Presentation & Demo Pitch (0-10)", 6, gbcS);
        sImpact = addSlider(slidersCard, "Real-world Impact & Social Value (0-10)", 8, gbcS);

        // Score display and submit button
        JPanel scorePanel = new JPanel(new BorderLayout());
        scorePanel.setBackground(Theme.PANEL_BG);
        scorePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR));

        lblTotalScore = new JLabel("Total Score: 0 / 50");
        lblTotalScore.setFont(Theme.FONT_TITLE);
        lblTotalScore.setForeground(Theme.PRIMARY);
        scorePanel.add(lblTotalScore, BorderLayout.WEST);

        btnSubmit = new CustomButton("Submit Score", CustomButton.ButtonType.PRIMARY);
        btnSubmit.setPreferredSize(new Dimension(160, 38));
        scorePanel.add(btnSubmit, BorderLayout.EAST);

        gbcS.gridy = 10;
        gbcS.insets = new Insets(25, 0, 0, 0);
        slidersCard.add(scorePanel, gbcS);

        mainPanel.add(slidersCard, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Wire events
        projectCombo.addActionListener(e -> onProjectSelectionChange());
        btnSubmit.addActionListener(e -> saveOrUpdateScore());
        btnGithub.addActionListener(e -> openUrl(currentGithubUrl));
        btnPpt.addActionListener(e -> openUrl(currentPptUrl));

        // Listener for dynamic total calculation
        ChangeListener cl = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                calculateTotalScore();
            }
        };
        sInnovation.addChangeListener(cl);
        sTechnical.addChangeListener(cl);
        sUiux.addChangeListener(cl);
        sPresentation.addChangeListener(cl);
        sImpact.addChangeListener(cl);

        // Load combo with recommendations sorted
        loadJudgeAssignments();
    }

    private JSlider addSlider(JPanel panel, String labelText, int gridY, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_BODY_BOLD);
        label.setForeground(Theme.TEXT_SECONDARY);
        gbc.gridy = gridY;
        gbc.insets = new Insets(6, 0, 0, 0);
        panel.add(label, gbc);

        JSlider slider = new JSlider(0, 10, 0);
        slider.setBackground(Theme.PANEL_BG);
        slider.setMajorTickSpacing(2);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        
        // Custom tick labels font
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = 0; i <= 10; i += 2) {
            JLabel tickLabel = new JLabel(String.valueOf(i));
            tickLabel.setFont(Theme.FONT_SMALL);
            tickLabel.setForeground(Theme.TEXT_LIGHT);
            labelTable.put(i, tickLabel);
        }
        slider.setLabelTable(labelTable);

        gbc.gridy = gridY + 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(slider, gbc);

        return slider;
    }

    private void calculateTotalScore() {
        currentTotalScore = sInnovation.getValue() + sTechnical.getValue() + sUiux.getValue() + sPresentation.getValue() + sImpact.getValue();
        lblTotalScore.setText("Total Score: " + currentTotalScore + " / 50");
    }

    private void loadAllEvaluations() {
        tableModel.setRowCount(0);
        String sql = "SELECT e.evaluation_id, t.team_name, p.title, j.name, " +
                     "e.innovation, e.technical, e.uiux, e.presentation, e.impact, e.total_score " +
                     "FROM evaluations e " +
                     "JOIN projects p ON p.project_id = e.project_id " +
                     "JOIN teams t ON t.team_id = p.team_id " +
                     "JOIN judges j ON j.judge_id = e.judge_id " +
                     "ORDER BY e.total_score DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("evaluation_id"),
                    rs.getString("team_name"),
                    rs.getString("title"),
                    rs.getString("name"),
                    rs.getInt("innovation"),
                    rs.getInt("technical"),
                    rs.getInt("uiux"),
                    rs.getInt("presentation"),
                    rs.getInt("impact"),
                    rs.getInt("total_score")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadJudgeAssignments() {
        projectCombo.removeAllItems();
        
        // Fetch recommendations (matches expertise domain first)
        List<Recommender.Recommendation> list = Recommender.getRecommendations(judgeId);
        
        // Filter: only show projects that are actually assigned to this judge
        List<Integer> assignedProjIds = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT project_id FROM assignments WHERE judge_id = ?")) {
            ps.setInt(1, judgeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignedProjIds.add(rs.getInt("project_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int count = 0;
        for (Recommender.Recommendation rec : list) {
            if (assignedProjIds.contains(rec.getProject().getProjectId())) {
                projectCombo.addItem(rec);
                count++;
            }
        }

        if (count == 0) {
            txtProjDetails.setText("No projects currently assigned to you for grading.");
            btnSubmit.setEnabled(false);
            sInnovation.setEnabled(false);
            sTechnical.setEnabled(false);
            sUiux.setEnabled(false);
            sPresentation.setEnabled(false);
            sImpact.setEnabled(false);
        } else {
            btnSubmit.setEnabled(true);
            sInnovation.setEnabled(true);
            sTechnical.setEnabled(true);
            sUiux.setEnabled(true);
            sPresentation.setEnabled(true);
            sImpact.setEnabled(true);
            onProjectSelectionChange();
        }
    }

    private void onProjectSelectionChange() {
        Recommender.Recommendation rec = (Recommender.Recommendation) projectCombo.getSelectedItem();
        if (rec == null) return;

        int projId = rec.getProject().getProjectId();
        
        // Update details text and link URLs
        txtProjDetails.setText("Title: " + rec.getProject().getTitle() + "\n" +
                              "Domain: " + rec.getProject().getDomain() + "\n\n" +
                              "Description:\n" + rec.getProject().getDescription());

        currentGithubUrl = rec.getProject().getGithubLink();
        currentPptUrl = rec.getProject().getPptLink();

        btnGithub.setEnabled(currentGithubUrl != null && !currentGithubUrl.trim().isEmpty());
        btnPpt.setEnabled(currentPptUrl != null && !currentPptUrl.trim().isEmpty());

        // Check if judge already evaluated this project
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM evaluations WHERE project_id = ? AND judge_id = ?")) {
            ps.setInt(1, projId);
            ps.setInt(2, judgeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sInnovation.setValue(rs.getInt("innovation"));
                    sTechnical.setValue(rs.getInt("technical"));
                    sUiux.setValue(rs.getInt("uiux"));
                    sPresentation.setValue(rs.getInt("presentation"));
                    sImpact.setValue(rs.getInt("impact"));
                    btnSubmit.setText("Update Score");
                } else {
                    // Reset to 0
                    sInnovation.setValue(0);
                    sTechnical.setValue(0);
                    sUiux.setValue(0);
                    sPresentation.setValue(0);
                    sImpact.setValue(0);
                    btnSubmit.setText("Submit Score");
                }
            }
            calculateTotalScore();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveOrUpdateScore() {
        Recommender.Recommendation rec = (Recommender.Recommendation) projectCombo.getSelectedItem();
        if (rec == null) return;

        int projId = rec.getProject().getProjectId();
        int inno = sInnovation.getValue();
        int tech = sTechnical.getValue();
        int uiux = sUiux.getValue();
        int pres = sPresentation.getValue();
        int imp = sImpact.getValue();
        int total = currentTotalScore;

        try (Connection conn = DatabaseHelper.getConnection()) {
            boolean isInsert = btnSubmit.getText().equals("Submit Score");
            String sql;
            
            if (isInsert) {
                sql = "INSERT INTO evaluations (project_id, judge_id, innovation, technical, uiux, presentation, impact, total_score) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE evaluations SET innovation = ?, technical = ?, uiux = ?, presentation = ?, impact = ?, total_score = ? WHERE project_id = ? AND judge_id = ?";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (isInsert) {
                    ps.setInt(1, projId);
                    ps.setInt(2, judgeId);
                    ps.setInt(3, inno);
                    ps.setInt(4, tech);
                    ps.setInt(5, uiux);
                    ps.setInt(6, pres);
                    ps.setInt(7, imp);
                    ps.setInt(8, total);
                } else {
                    ps.setInt(1, inno);
                    ps.setInt(2, tech);
                    ps.setInt(3, uiux);
                    ps.setInt(4, pres);
                    ps.setInt(5, imp);
                    ps.setInt(6, total);
                    ps.setInt(7, projId);
                    ps.setInt(8, judgeId);
                }
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Evaluation saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            onProjectSelectionChange();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openUrl(String url) {
        if (url == null || url.trim().isEmpty()) return;
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not open link in browser: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
