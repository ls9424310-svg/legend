package com.hackathon.manager.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import com.hackathon.manager.db.DatabaseHelper;
import com.hackathon.manager.ui.Theme;
import com.hackathon.manager.ui.components.CustomButton;
import com.hackathon.manager.ui.components.CustomTable;

public class LeaderboardPanel extends JPanel {
    private JComboBox<String> domainFilterCombo;
    private CustomTable table;
    private DefaultTableModel tableModel;
    private CustomButton btnRefresh;

    public LeaderboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Live Leaderboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        setupUI();
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setBackground(Color.WHITE);

        JLabel lblFilter = new JLabel("Filter by Domain:");
        lblFilter.setFont(Theme.FONT_BODY_BOLD);
        lblFilter.setForeground(Theme.TEXT_SECONDARY);
        filterPanel.add(lblFilter);

        String[] domains = {"All Domains", "AI/ML", "Web Development", "Android", "IoT", "Cyber Security", "Cloud Computing"};
        domainFilterCombo = new JComboBox<>(domains);
        domainFilterCombo.setFont(Theme.FONT_BODY);
        domainFilterCombo.setBackground(Color.WHITE);
        domainFilterCombo.setPreferredSize(new Dimension(200, 36));
        filterPanel.add(domainFilterCombo);

        btnRefresh = new CustomButton("Refresh", CustomButton.ButtonType.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(100, 36));
        filterPanel.add(btnRefresh);

        mainPanel.add(filterPanel, BorderLayout.NORTH);

        // Leaderboard Table
        String[] columns = {"Rank", "Team Name", "Project Title", "College", "Domain", "Average Score"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new CustomTable(tableModel);
        JScrollPane scroll = CustomTable.createScrollPane(table);
        mainPanel.add(scroll, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Wire events
        domainFilterCombo.addActionListener(e -> loadLeaderboardData());
        btnRefresh.addActionListener(e -> loadLeaderboardData());

        // Load initially
        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        tableModel.setRowCount(0);
        String selectedDomain = (String) domainFilterCombo.getSelectedItem();
        boolean hasFilter = selectedDomain != null && !"All Domains".equals(selectedDomain);

        String sql = "SELECT t.team_name, p.title, t.college, p.domain, AVG(e.total_score) as avg_score " +
                     "FROM evaluations e " +
                     "JOIN projects p ON p.project_id = e.project_id " +
                     "JOIN teams t ON t.team_id = p.team_id ";
        
        if (hasFilter) {
            sql += "WHERE p.domain = ? ";
        }
        
        sql += "GROUP BY t.team_id ORDER BY avg_score DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (hasFilter) {
                ps.setString(1, selectedDomain);
            }

            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    double score = rs.getDouble("avg_score");
                    tableModel.addRow(new Object[]{
                        "#" + rank,
                        rs.getString("team_name"),
                        rs.getString("title"),
                        rs.getString("college"),
                        rs.getString("domain"),
                        String.format("%.2f / 50", score)
                    });
                    rank++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
