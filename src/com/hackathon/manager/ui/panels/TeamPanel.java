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
import javax.swing.BorderFactory;
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

public class TeamPanel extends JPanel {
    private final String role;
    private final int teamId; // for Team Leader

    // UI elements
    private CustomTextField txtName, txtLeader, txtCollege, txtPhone, txtEmail, txtSearch;
    private CustomTable table;
    private DefaultTableModel tableModel;
    private CustomButton btnSave, btnUpdate, btnDelete, btnSearch;

    private int selectedTeamId = -1;

    public TeamPanel(String role, int teamId) {
        this.role = role;
        this.teamId = teamId;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("Admin".equals(role) ? "Team Management" : "My Team Details");
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
        // Main split panel: Left is CRUD Form, Right is Table
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

        // Form Fields
        txtName = addFormField(formPanel, "Team Name", 0, gbc);
        txtLeader = addFormField(formPanel, "Team Leader Name", 2, gbc);
        txtCollege = addFormField(formPanel, "College Name", 4, gbc);
        txtPhone = addFormField(formPanel, "Contact Number", 6, gbc);
        txtEmail = addFormField(formPanel, "Email ID", 8, gbc);

        // Buttons Panel
        JPanel btnGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        btnGrid.setBackground(Theme.PANEL_BG);

        btnSave = new CustomButton("Save", CustomButton.ButtonType.PRIMARY);
        btnUpdate = new CustomButton("Update", CustomButton.ButtonType.SECONDARY);
        btnDelete = new CustomButton("Delete", CustomButton.ButtonType.DANGER);

        btnGrid.add(btnSave);
        btnGrid.add(btnUpdate);
        btnGrid.add(btnDelete);

        gbc.gridy = 10;
        gbc.insets = new Insets(15, 0, 0, 0);
        formPanel.add(btnGrid, gbc);

        mainPanel.add(formPanel, BorderLayout.WEST);

        // 2. Right Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);

        // Search Bar Row
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(Color.WHITE);
        txtSearch = new CustomTextField("Search by Team, Leader, or College...");
        txtSearch.setPreferredSize(new Dimension(280, 36));
        btnSearch = new CustomButton("Search", CustomButton.ButtonType.SECONDARY);
        btnSearch.setPreferredSize(new Dimension(100, 36));
        
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        tableContainer.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Team Name", "Leader", "College", "Email", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new CustomTable(tableModel);
        JScrollPane scroll = CustomTable.createScrollPane(table);
        tableContainer.add(scroll, BorderLayout.CENTER);

        mainPanel.add(tableContainer, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Wire Event Listeners
        btnSave.addActionListener(e -> saveTeam());
        btnUpdate.addActionListener(e -> updateTeam());
        btnDelete.addActionListener(e -> deleteTeam());
        btnSearch.addActionListener(e -> loadTeams(txtSearch.getText().trim()));
        
        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                selectedTeamId = (int) table.getValueAt(selectedRow, 0);
                txtName.setText((String) table.getValueAt(selectedRow, 1));
                txtLeader.setText((String) table.getValueAt(selectedRow, 2));
                txtCollege.setText((String) table.getValueAt(selectedRow, 3));
                txtEmail.setText((String) table.getValueAt(selectedRow, 4));
                txtPhone.setText((String) table.getValueAt(selectedRow, 5));
            }
        });

        // Load initially
        loadTeams("");
    }

    private void setupTeamLeaderUI() {
        // Team Leader just gets a beautiful styled card in the center to view/update details
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        centerWrapper.setBackground(Color.WHITE);

        RoundedPanel formPanel = new RoundedPanel(10, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
        formPanel.setPreferredSize(new Dimension(420, 480));
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        JLabel welcomeLabel = new JLabel("Update Your Team Profile", JLabel.CENTER);
        welcomeLabel.setFont(Theme.FONT_SUBTITLE);
        welcomeLabel.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(welcomeLabel, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);
        txtName = addFormField(formPanel, "Team Name", 1, gbc);
        txtLeader = addFormField(formPanel, "Team Leader Name", 3, gbc);
        txtCollege = addFormField(formPanel, "College Name", 5, gbc);
        txtPhone = addFormField(formPanel, "Contact Number", 7, gbc);
        txtEmail = addFormField(formPanel, "Email ID", 9, gbc);
        txtEmail.setEditable(false); // cannot change registered email
        txtEmail.setEnabled(false);

        btnUpdate = new CustomButton("Update Details", CustomButton.ButtonType.PRIMARY);
        btnUpdate.setPreferredSize(new Dimension(200, 38));
        gbc.gridy = 11;
        gbc.insets = new Insets(20, 0, 0, 0);
        formPanel.add(btnUpdate, gbc);

        centerWrapper.add(formPanel);
        add(centerWrapper, BorderLayout.CENTER);

        btnUpdate.addActionListener(e -> updateTeam());

        // Load details
        loadSingleTeam();
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

    private void loadTeams(String search) {
        tableModel.setRowCount(0);
        String query = "SELECT * FROM teams";
        if (!search.isEmpty()) {
            query += " WHERE team_name LIKE ? OR leader_name LIKE ? OR college LIKE ?";
        }
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            if (!search.isEmpty()) {
                String searchPattern = "%" + search + "%";
                ps.setString(1, searchPattern);
                ps.setString(2, searchPattern);
                ps.setString(3, searchPattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("team_id"),
                        rs.getString("team_name"),
                        rs.getString("leader_name"),
                        rs.getString("college"),
                        rs.getString("email"),
                        rs.getString("phone")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSingleTeam() {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM teams WHERE team_id = ?")) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtName.setText(rs.getString("team_name"));
                    txtLeader.setText(rs.getString("leader_name"));
                    txtCollege.setText(rs.getString("college"));
                    txtPhone.setText(rs.getString("phone"));
                    txtEmail.setText(rs.getString("email"));
                    selectedTeamId = teamId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveTeam() {
        String name = txtName.getText().trim();
        String leader = txtLeader.getText().trim();
        String college = txtCollege.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || leader.isEmpty() || college.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO teams (team_name, leader_name, college, phone, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, leader);
            ps.setString(3, college);
            ps.setString(4, phone);
            ps.setString(5, email);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Team registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadTeams("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTeam() {
        int targetId = "Admin".equals(role) ? selectedTeamId : teamId;
        if (targetId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a team from the table to update.", "Selection Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        String leader = txtLeader.getText().trim();
        String college = txtCollege.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || leader.isEmpty() || college.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE teams SET team_name = ?, leader_name = ?, college = ?, phone = ?, email = ? WHERE team_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, leader);
            ps.setString(3, college);
            ps.setString(4, phone);
            ps.setString(5, email);
            ps.setInt(6, targetId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Team details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            if ("Admin".equals(role)) {
                clearFields();
                loadTeams("");
            } else {
                loadSingleTeam();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTeam() {
        if (selectedTeamId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a team from the table to delete.", "Selection Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this team?\nThis will delete all its participants and projects.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM teams WHERE team_id = ?")) {
                ps.setInt(1, selectedTeamId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Team deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadTeams("");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Deletion failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        txtName.setText("");
        txtLeader.setText("");
        txtCollege.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        selectedTeamId = -1;
    }
}
