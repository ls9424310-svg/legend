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

public class ParticipantPanel extends JPanel {
    private final String role;
    private final int teamId;

    // UI elements
    private JComboBox<String> teamCombo;
    private CustomTextField txtName, txtBranch, txtSemester, txtEmail;
    private CustomTable table;
    private DefaultTableModel tableModel;
    private CustomButton btnAdd, btnRemove;

    // Map combo label to team_id
    private final List<Integer> teamIdsList = new ArrayList<>();

    public ParticipantPanel(String role, int teamId) {
        this.role = role;
        this.teamId = teamId;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Admin".equals(role) ? "Participant Management" : "Team Member Registry");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
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

        // Form fields
        JLabel lblTeam = new JLabel("Select Team");
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

        txtName = addFormField(formPanel, "Student Name", 2, gbc);
        txtBranch = addFormField(formPanel, "Branch / Stream", 4, gbc);
        txtSemester = addFormField(formPanel, "Semester", 6, gbc);
        txtEmail = addFormField(formPanel, "Student Email", 8, gbc);

        // Buttons
        JPanel btnGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnGrid.setBackground(Theme.PANEL_BG);

        btnAdd = new CustomButton("Add Member", CustomButton.ButtonType.PRIMARY);
        btnRemove = new CustomButton("Remove", CustomButton.ButtonType.DANGER);
        btnGrid.add(btnAdd);
        btnGrid.add(btnRemove);

        gbc.gridy = 10;
        gbc.insets = new Insets(20, 0, 0, 0);
        formPanel.add(btnGrid, gbc);

        mainPanel.add(formPanel, BorderLayout.WEST);

        // 2. Right Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);

        String[] columns = {"ID", "Team Name", "Name", "Branch", "Semester", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new CustomTable(tableModel);
        JScrollPane scroll = CustomTable.createScrollPane(table);
        tableContainer.add(scroll, BorderLayout.CENTER);

        mainPanel.add(tableContainer, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Load combobox
        loadTeamsCombo();

        // Wire events
        btnAdd.addActionListener(e -> addParticipant());
        btnRemove.addActionListener(e -> removeParticipant());
        teamCombo.addActionListener(e -> filterTableBySelectedTeam());
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

    private void loadTeamsCombo() {
        teamCombo.removeAllItems();
        teamIdsList.clear();

        if ("Admin".equals(role)) {
            teamCombo.addItem("All Teams");
            teamIdsList.add(-1);

            try (Connection conn = DatabaseHelper.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT team_id, team_name FROM teams")) {
                while (rs.next()) {
                    teamCombo.addItem(rs.getString("team_name"));
                    teamIdsList.add(rs.getInt("team_id"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Team Leader
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT team_id, team_name FROM teams WHERE team_id = ?")) {
                ps.setInt(1, teamId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        teamCombo.addItem(rs.getString("team_name"));
                        teamIdsList.add(rs.getInt("team_id"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            teamCombo.setEnabled(false); // Locked for Team Leaders
        }
        
        // Initial load
        filterTableBySelectedTeam();
    }

    private void filterTableBySelectedTeam() {
        int selIdx = teamCombo.getSelectedIndex();
        if (selIdx == -1 || selIdx >= teamIdsList.size()) return;

        int selectedId = teamIdsList.get(selIdx);
        tableModel.setRowCount(0);

        String sql = "SELECT p.*, t.team_name FROM participants p JOIN teams t ON t.team_id = p.team_id";
        if (selectedId != -1) {
            sql += " WHERE p.team_id = ?";
        }

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (selectedId != -1) {
                ps.setInt(1, selectedId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("participant_id"),
                        rs.getString("team_name"),
                        rs.getString("name"),
                        rs.getString("branch"),
                        rs.getString("semester"),
                        rs.getString("email")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addParticipant() {
        int selIdx = teamCombo.getSelectedIndex();
        if (selIdx == -1 || ("Admin".equals(role) && teamIdsList.get(selIdx) == -1)) {
            JOptionPane.showMessageDialog(this, "Please select a specific team to add member to.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int targetTeamId = teamIdsList.get(selIdx);
        String name = txtName.getText().trim();
        String branch = txtBranch.getText().trim();
        String semester = txtSemester.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || branch.isEmpty() || semester.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required to add a member.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Limit check: college hackathons usually limit members per team to 4
        try (Connection conn = DatabaseHelper.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM participants WHERE team_id = ?")) {
                ps.setInt(1, targetTeamId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) >= 4) {
                        JOptionPane.showMessageDialog(this, "A team cannot have more than 4 members.", "Limit Reached", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            // Insert member
            String sql = "INSERT INTO participants (team_id, name, branch, semester, email) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, targetTeamId);
                ps.setString(2, name);
                ps.setString(3, branch);
                ps.setString(4, semester);
                ps.setString(5, email);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Team member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            txtName.setText("");
            txtBranch.setText("");
            txtSemester.setText("");
            txtEmail.setText("");

            filterTableBySelectedTeam();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Insertion failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void removeParticipant() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a participant from the table to remove.", "Selection Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int participantId = (int) table.getValueAt(selectedRow, 0);
        String pName = (String) table.getValueAt(selectedRow, 2);

        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove " + pName + " from the team?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM participants WHERE participant_id = ?")) {
                ps.setInt(1, participantId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Participant removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                filterTableBySelectedTeam();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Removal failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
