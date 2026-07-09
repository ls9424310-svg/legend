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

public class JudgePanel extends JPanel {
    private CustomTextField txtName, txtEmail, txtPhone, txtPassword;
    private JComboBox<String> domainCombo;
    private CustomTable table;
    private DefaultTableModel tableModel;
    private CustomButton btnSave, btnUpdate, btnDelete;
    private int selectedJudgeId = -1;

    public JudgePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Judge Management");
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

        // Form Fields
        txtName = addFormField(formPanel, "Judge Name", 0, gbc);

        JLabel lblDomain = new JLabel("Expertise Domain");
        lblDomain.setFont(Theme.FONT_BODY_BOLD);
        lblDomain.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 2;
        formPanel.add(lblDomain, gbc);

        String[] domains = {"AI/ML", "Web Development", "Android", "IoT", "Cyber Security", "Cloud Computing"};
        domainCombo = new JComboBox<>(domains);
        domainCombo.setFont(Theme.FONT_BODY);
        domainCombo.setBackground(Color.WHITE);
        domainCombo.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 3;
        formPanel.add(domainCombo, gbc);

        txtEmail = addFormField(formPanel, "Email / Username", 4, gbc);
        txtPhone = addFormField(formPanel, "Contact Number", 6, gbc);
        txtPassword = addFormField(formPanel, "Access Password", 8, gbc);

        // Buttons
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
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);

        String[] columns = {"ID", "Name", "Domain", "Email", "Phone", "Password"};
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
        btnSave.addActionListener(e -> saveJudge());
        btnUpdate.addActionListener(e -> updateJudge());
        btnDelete.addActionListener(e -> deleteJudge());

        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                selectedJudgeId = (int) table.getValueAt(selectedRow, 0);
                txtName.setText((String) table.getValueAt(selectedRow, 1));
                domainCombo.setSelectedItem((String) table.getValueAt(selectedRow, 2));
                txtEmail.setText((String) table.getValueAt(selectedRow, 3));
                txtPhone.setText((String) table.getValueAt(selectedRow, 4));
                txtPassword.setText((String) table.getValueAt(selectedRow, 5));
            }
        });

        // Load initially
        loadJudges();
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

    private void loadJudges() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM judges")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("judge_id"),
                    rs.getString("name"),
                    rs.getString("domain"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("password")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveJudge() {
        String name = txtName.getText().trim();
        String domain = (String) domainCombo.getSelectedItem();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String password = txtPassword.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Email and Password are required.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO judges (name, domain, email, phone, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, domain);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, password);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Judge registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadJudges();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateJudge() {
        if (selectedJudgeId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a judge from the table to update.", "Selection Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        String domain = (String) domainCombo.getSelectedItem();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String password = txtPassword.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Email and Password are required.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE judges SET name = ?, domain = ?, email = ?, phone = ?, password = ? WHERE judge_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, domain);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, password);
            ps.setInt(6, selectedJudgeId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Judge details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadJudges();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteJudge() {
        if (selectedJudgeId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a judge from the table to delete.", "Selection Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this judge?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM judges WHERE judge_id = ?")) {
                ps.setInt(1, selectedJudgeId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Judge removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                loadJudges();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Deletion failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtPassword.setText("");
        selectedJudgeId = -1;
    }
}
