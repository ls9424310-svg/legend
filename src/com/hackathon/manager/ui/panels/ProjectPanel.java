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
import javax.swing.table.DefaultTableModel;
import com.hackathon.manager.db.DatabaseHelper;
import com.hackathon.manager.ui.Theme;
import com.hackathon.manager.ui.components.CustomButton;
import com.hackathon.manager.ui.components.CustomTable;
import com.hackathon.manager.ui.components.CustomTextField;
import com.hackathon.manager.ui.components.RoundedPanel;

public class ProjectPanel extends JPanel {
    private final String role;
    private final int teamId;

    // UI elements
    private JComboBox<String> teamCombo;
    private JComboBox<String> domainCombo;
    private CustomTextField txtTitle, txtGithub, txtPpt, txtDesc;
    private CustomTable table;
    private DefaultTableModel tableModel;
    private CustomButton btnSubmit, btnUpdate;

    private final List<Integer> teamIds = new ArrayList<>();
    private int selectedProjectId = -1;

    public ProjectPanel(String role, int teamId) {
        this.role = role;
        this.teamId = teamId;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Admin".equals(role) ? "Project Submissions" : "Upload Project");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        if ("Admin".equals(role)) {
            setupAdminUI();
        } else {
            setupTeamLeaderUI();
        }
    }

    private void setupAdminUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(Color.WHITE);

        // 1. Left Form Panel (Details)
        RoundedPanel formPanel = new RoundedPanel(10, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
        formPanel.setPreferredSize(new Dimension(320, 500));
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        // Team Selector
        JLabel lblTeam = new JLabel("Team Name");
        lblTeam.setFont(Theme.FONT_BODY_BOLD);
        lblTeam.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 0;
        formPanel.add(lblTeam, gbc);

        teamCombo = new JComboBox<>();
        teamCombo.setFont(Theme.FONT_BODY);
        teamCombo.setBackground(Color.WHITE);
        teamCombo.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 1;
        formPanel.add(teamCombo, gbc);

        txtTitle = addFormField(formPanel, "Project Title", 2, gbc);

        JLabel lblDomain = new JLabel("Domain");
        lblDomain.setFont(Theme.FONT_BODY_BOLD);
        lblDomain.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 4;
        formPanel.add(lblDomain, gbc);

        String[] domains = {"AI/ML", "Web Development", "Android", "IoT", "Cyber Security", "Cloud Computing"};
        domainCombo = new JComboBox<>(domains);
        domainCombo.setFont(Theme.FONT_BODY);
        domainCombo.setBackground(Color.WHITE);
        domainCombo.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 5;
        formPanel.add(domainCombo, gbc);

        txtDesc = addFormField(formPanel, "Description", 6, gbc);
        txtGithub = addFormField(formPanel, "GitHub Link", 8, gbc);
        txtPpt = addFormField(formPanel, "PPT Link", 10, gbc);

        // Buttons
        JPanel btnGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnGrid.setBackground(Theme.PANEL_BG);

        btnUpdate = new CustomButton("Update", CustomButton.ButtonType.PRIMARY);
        btnGrid.add(btnUpdate);
        gbc.gridy = 12;
        gbc.insets = new Insets(15, 0, 0, 0);
        formPanel.add(btnGrid, gbc);

        mainPanel.add(formPanel, BorderLayout.WEST);

        // 2. Right Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);

        String[] columns = {"ID", "Team Name", "Project Title", "Domain", "GitHub", "PPT"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new CustomTable(tableModel);
        JScrollPane scroll = CustomTable.createScrollPane(table);
        tableContainer.add(scroll, BorderLayout.CENTER);

        mainPanel.add(tableContainer, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Wire Events
        btnUpdate.addActionListener(e -> updateProject());
        
        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                selectedProjectId = (int) table.getValueAt(selectedRow, 0);
                String tName = (String) table.getValueAt(selectedRow, 1);
                teamCombo.setSelectedItem(tName);
                txtTitle.setText((String) table.getValueAt(selectedRow, 2));
                domainCombo.setSelectedItem((String) table.getValueAt(selectedRow, 3));
                
                // Fetch full description and links from database
                fetchProjectDetails(selectedProjectId);
            }
        });

        // Load initially
        loadTeamsList();
        loadProjectsTable();
    }

    private void setupTeamLeaderUI() {
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        centerWrapper.setBackground(Color.WHITE);

        RoundedPanel formPanel = new RoundedPanel(10, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
        formPanel.setPreferredSize(new Dimension(420, 520));
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        JLabel welcomeLabel = new JLabel("Upload/Edit Project Details", JLabel.CENTER);
        welcomeLabel.setFont(Theme.FONT_SUBTITLE);
        welcomeLabel.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(welcomeLabel, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);
        txtTitle = addFormField(formPanel, "Project Title", 1, gbc);

        JLabel lblDomain = new JLabel("Domain Category");
        lblDomain.setFont(Theme.FONT_BODY_BOLD);
        lblDomain.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 3;
        formPanel.add(lblDomain, gbc);

        String[] domains = {"AI/ML", "Web Development", "Android", "IoT", "Cyber Security", "Cloud Computing"};
        domainCombo = new JComboBox<>(domains);
        domainCombo.setFont(Theme.FONT_BODY);
        domainCombo.setBackground(Color.WHITE);
        domainCombo.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 4;
        formPanel.add(domainCombo, gbc);

        txtDesc = addFormField(formPanel, "Project Description", 5, gbc);
        txtGithub = addFormField(formPanel, "GitHub Link", 7, gbc);
        txtPpt = addFormField(formPanel, "PPT Slides Link", 9, gbc);

        btnSubmit = new CustomButton("Submit Project", CustomButton.ButtonType.PRIMARY);
        btnSubmit.setPreferredSize(new Dimension(200, 38));
        gbc.gridy = 11;
        gbc.insets = new Insets(20, 0, 0, 0);
        formPanel.add(btnSubmit, gbc);

        centerWrapper.add(formPanel);
        add(centerWrapper, BorderLayout.CENTER);

        btnSubmit.addActionListener(e -> submitOrUpdateOwnProject());

        // Check if already submitted and load
        loadOwnProject();
    }

    private CustomTextField addFormField(JPanel panel, String labelStr, int gridY, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelStr);
        label.setFont(Theme.FONT_SMALL_BOLD);
        label.setForeground(Theme.TEXT_SECONDARY);
        gbc.gridy = gridY;
        panel.add(label, gbc);

        CustomTextField txt = new CustomTextField("Enter " + labelStr.toLowerCase());
        txt.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = gridY + 1;
        panel.add(txt, gbc);

        return txt;
    }

    private void loadTeamsList() {
        teamCombo.removeAllItems();
        teamIds.clear();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT team_id, team_name FROM teams")) {
            while (rs.next()) {
                teamCombo.addItem(rs.getString("team_name"));
                teamIds.add(rs.getInt("team_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProjectsTable() {
        tableModel.setRowCount(0);
        String sql = "SELECT p.*, t.team_name FROM projects p JOIN teams t ON t.team_id = p.team_id";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("project_id"),
                    rs.getString("team_name"),
                    rs.getString("title"),
                    rs.getString("domain"),
                    rs.getString("github_link"),
                    rs.getString("ppt_link")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchProjectDetails(int projId) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT description, github_link, ppt_link FROM projects WHERE project_id = ?")) {
            ps.setInt(1, projId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtDesc.setText(rs.getString("description"));
                    txtGithub.setText(rs.getString("github_link"));
                    txtPpt.setText(rs.getString("ppt_link"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadOwnProject() {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM projects WHERE team_id = ?")) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    selectedProjectId = rs.getInt("project_id");
                    txtTitle.setText(rs.getString("title"));
                    domainCombo.setSelectedItem(rs.getString("domain"));
                    txtDesc.setText(rs.getString("description"));
                    txtGithub.setText(rs.getString("github_link"));
                    txtPpt.setText(rs.getString("ppt_link"));
                    btnSubmit.setText("Update Submission");
                } else {
                    selectedProjectId = -1;
                    btnSubmit.setText("Submit Project");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitOrUpdateOwnProject() {
        String title = txtTitle.getText().trim();
        String domain = (String) domainCombo.getSelectedItem();
        String desc = txtDesc.getText().trim();
        String github = txtGithub.getText().trim();
        String ppt = txtPpt.getText().trim();

        if (title.isEmpty() || desc.isEmpty() || github.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, Description, and GitHub URL are required.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            if (selectedProjectId == -1) {
                // New Submission
                String sql = "INSERT INTO projects (team_id, title, domain, description, github_link, ppt_link) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, teamId);
                    ps.setString(2, title);
                    ps.setString(3, domain);
                    ps.setString(4, desc);
                    ps.setString(5, github);
                    ps.setString(6, ppt);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Project uploaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Update
                String sql = "UPDATE projects SET title = ?, domain = ?, description = ?, github_link = ?, ppt_link = ? WHERE project_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, title);
                    ps.setString(2, domain);
                    ps.setString(3, desc);
                    ps.setString(4, github);
                    ps.setString(5, ppt);
                    ps.setInt(6, selectedProjectId);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Project submission updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            loadOwnProject();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Submission failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateProject() {
        if (selectedProjectId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a project from the table to update.", "Selection Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = txtTitle.getText().trim();
        String domain = (String) domainCombo.getSelectedItem();
        String desc = txtDesc.getText().trim();
        String github = txtGithub.getText().trim();
        String ppt = txtPpt.getText().trim();

        int selIdx = teamCombo.getSelectedIndex();
        int tId = teamIds.get(selIdx);

        if (title.isEmpty() || desc.isEmpty() || github.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, Description, and GitHub URL are required.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE projects SET team_id = ?, title = ?, domain = ?, description = ?, github_link = ?, ppt_link = ? WHERE project_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tId);
            ps.setString(2, title);
            ps.setString(3, domain);
            ps.setString(4, desc);
            ps.setString(5, github);
            ps.setString(6, ppt);
            ps.setInt(7, selectedProjectId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Project details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadProjectsTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
