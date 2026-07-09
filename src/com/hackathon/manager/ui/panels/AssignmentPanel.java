package com.hackathon.manager.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import com.hackathon.manager.db.DatabaseHelper;
import com.hackathon.manager.ui.Theme;
import com.hackathon.manager.ui.components.CustomButton;
import com.hackathon.manager.ui.components.CustomTable;
import com.hackathon.manager.ui.components.RoundedPanel;
import com.hackathon.manager.util.AutoAssigner;

public class AssignmentPanel extends JPanel {
    private JComboBox<String> projectCombo;
    private JComboBox<String> judgeCombo;
    private JTextArea txtRecommendations;
    private CustomTable table;
    private DefaultTableModel tableModel;
    private CustomButton btnAssign, btnRemove, btnAutoAssign;

    private final List<Integer> projectIds = new ArrayList<>();
    private final List<String> projectDomains = new ArrayList<>();
    private final List<Integer> judgeIds = new ArrayList<>();

    public AssignmentPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Judge Assignments");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);

        // Auto assign button in header
        btnAutoAssign = new CustomButton("Auto Assign Judges", CustomButton.ButtonType.PRIMARY);
        btnAutoAssign.setPreferredSize(new Dimension(170, 36));
        headerPanel.add(btnAutoAssign, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        setupUI();
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(Color.WHITE);

        // 1. Left Form Panel
        RoundedPanel formPanel = new RoundedPanel(10, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
        formPanel.setPreferredSize(new Dimension(320, 500));
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        // Project selector
        JLabel lblProj = new JLabel("Select Project");
        lblProj.setFont(Theme.FONT_BODY_BOLD);
        lblProj.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 0;
        formPanel.add(lblProj, gbc);

        projectCombo = new JComboBox<>();
        projectCombo.setFont(Theme.FONT_BODY);
        projectCombo.setBackground(Color.WHITE);
        projectCombo.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 1;
        formPanel.add(projectCombo, gbc);

        // Judge selector
        JLabel lblJudge = new JLabel("Select Judge");
        lblJudge.setFont(Theme.FONT_BODY_BOLD);
        lblJudge.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 2;
        formPanel.add(lblJudge, gbc);

        judgeCombo = new JComboBox<>();
        judgeCombo.setFont(Theme.FONT_BODY);
        judgeCombo.setBackground(Color.WHITE);
        judgeCombo.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 3;
        formPanel.add(judgeCombo, gbc);

        // Recommendations box
        JLabel lblRec = new JLabel("Recommended Judges (Domain Match)");
        lblRec.setFont(Theme.FONT_SMALL_BOLD);
        lblRec.setForeground(Theme.PRIMARY);
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 0, 2, 0);
        formPanel.add(lblRec, gbc);

        txtRecommendations = new JTextArea(4, 20);
        txtRecommendations.setEditable(false);
        txtRecommendations.setFont(Theme.FONT_SMALL);
        txtRecommendations.setBackground(new Color(239, 246, 255)); // light blue
        txtRecommendations.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        txtRecommendations.setLineWrap(true);
        txtRecommendations.setWrapStyleWord(true);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(txtRecommendations, gbc);

        // Buttons
        JPanel btnGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnGrid.setBackground(Theme.PANEL_BG);

        btnAssign = new CustomButton("Assign", CustomButton.ButtonType.PRIMARY);
        btnRemove = new CustomButton("Remove", CustomButton.ButtonType.DANGER);
        btnGrid.add(btnAssign);
        btnGrid.add(btnRemove);

        gbc.gridy = 6;
        gbc.insets = new Insets(15, 0, 0, 0);
        formPanel.add(btnGrid, gbc);

        mainPanel.add(formPanel, BorderLayout.WEST);

        // 2. Right Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);

        String[] columns = {"Assignment ID", "Project Title", "Domain", "Assigned Judge"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new CustomTable(tableModel);
        JScrollPane scroll = CustomTable.createScrollPane(table);
        tableContainer.add(scroll, BorderLayout.CENTER);

        mainPanel.add(tableContainer, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Load initially
        loadProjectsCombo();
        loadJudgesCombo();
        loadAssignmentsTable();

        // Wire events
        btnAssign.addActionListener(e -> assignJudge());
        btnRemove.addActionListener(e -> removeAssignment());
        btnAutoAssign.addActionListener(e -> triggerAutoAssign());
        projectCombo.addActionListener(e -> updateRecommendations());
    }

    private void loadProjectsCombo() {
        projectCombo.removeAllItems();
        projectIds.clear();
        projectDomains.clear();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT project_id, title, domain FROM projects")) {
            while (rs.next()) {
                projectCombo.addItem(rs.getString("title"));
                projectIds.add(rs.getInt("project_id"));
                projectDomains.add(rs.getString("domain"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadJudgesCombo() {
        judgeCombo.removeAllItems();
        judgeIds.clear();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT judge_id, name, domain FROM judges")) {
            while (rs.next()) {
                judgeCombo.addItem(rs.getString("name") + " (" + rs.getString("domain") + ")");
                judgeIds.add(rs.getInt("judge_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAssignmentsTable() {
        tableModel.setRowCount(0);
        String sql = "SELECT a.id, p.title, p.domain, j.name " +
                     "FROM assignments a " +
                     "JOIN projects p ON p.project_id = a.project_id " +
                     "JOIN judges j ON j.judge_id = a.judge_id";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("domain"),
                    rs.getString("name")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateRecommendations() {
        int idx = projectCombo.getSelectedIndex();
        if (idx == -1 || idx >= projectDomains.size()) {
            txtRecommendations.setText("No project selected.");
            return;
        }

        String domain = projectDomains.get(idx);
        StringBuilder sb = new StringBuilder();
        
        String sql = "SELECT name FROM judges WHERE domain = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, domain);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    sb.append("• ").append(rs.getString("name")).append("\n");
                }
                if (!found) {
                    sb.append("No judges found with expert domain matching: ").append(domain);
                }
            }
        } catch (Exception e) {
            sb.append("Error loading recommendations: ").append(e.getMessage());
        }
        
        txtRecommendations.setText(sb.toString().trim());
    }

    private void assignJudge() {
        int projIdx = projectCombo.getSelectedIndex();
        int judgeIdx = judgeCombo.getSelectedIndex();

        if (projIdx == -1 || judgeIdx == -1) {
            JOptionPane.showMessageDialog(this, "Please select both project and judge.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int projId = projectIds.get(projIdx);
        int judgeId = judgeIds.get(judgeIdx);

        try (Connection conn = DatabaseHelper.getConnection()) {
            // Check if already assigned
            String checkSql = "SELECT COUNT(*) FROM assignments WHERE project_id = ? AND judge_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, projId);
                ps.setInt(2, judgeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "This judge is already assigned to this project.", "Duplicate Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            // Insert assignment
            String insertSql = "INSERT INTO assignments (project_id, judge_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, projId);
                ps.setInt(2, judgeId);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Judge assigned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAssignmentsTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Assignment failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeAssignment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an assignment from the table to remove.", "Selection Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int assignId = (int) table.getValueAt(selectedRow, 0);

        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this judge assignment?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM assignments WHERE id = ?")) {
                ps.setInt(1, assignId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Assignment removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAssignmentsTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Removal failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void triggerAutoAssign() {
        String input = JOptionPane.showInputDialog(this, "Enter number of judges to assign per project:", "Auto Assigner", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int num = Integer.parseInt(input.trim());
            if (num <= 0) {
                JOptionPane.showMessageDialog(this, "Number of judges must be greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "WIPE all current assignments & scores and run auto-assignment?", "Confirm Wipe & Run", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean success = AutoAssigner.autoAssign(num);
            if (success) {
                JOptionPane.showMessageDialog(this, "Auto-assignment completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAssignmentsTable();
            } else {
                JOptionPane.showMessageDialog(this, "Auto-assignment failed. Ensure you have teams, projects, and judges registered.", "Assignment Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number entered.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
