package com.hackathon.manager.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import com.hackathon.manager.db.DatabaseHelper;
import com.hackathon.manager.ui.components.AnalyticsChart;
import com.hackathon.manager.ui.components.CustomButton;
import com.hackathon.manager.ui.components.RoundedPanel;
import com.hackathon.manager.ui.panels.*;

public class DashboardFrame extends JFrame {
    private final String role;
    private final int userId; // teamId for Team Leader, judgeId for Judge, 0 for Admin
    private final String userName;
    private final String extraInfo; // domain for Judge

    private JPanel sidebarPanel;
    private JPanel contentCardPanel;
    private CardLayout cardLayout;
    
    // Track selected sidebar button
    private CustomButton selectedMenuBtn = null;
    private final Map<String, CustomButton> menuButtons = new HashMap<>();

    public DashboardFrame(String role, int userId, String userName, String extraInfo) {
        super("College Hackathon Management System - Dashboard");
        this.role = role;
        this.userId = userId;
        this.userName = userName;
        this.extraInfo = extraInfo;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initialize two-panel layout
        setupSidebar();
        setupContentPanel();

        add(sidebarPanel, BorderLayout.WEST);
        add(contentCardPanel, BorderLayout.CENTER);
    }

    private void setupSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(250, 700));
        sidebarPanel.setBackground(Theme.PANEL_BG);
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_COLOR));

        // 1. App Header/Brand
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setBackground(Theme.PANEL_BG);
        brandPanel.setMaximumSize(new Dimension(250, 75));
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel brandLabel = new JLabel("HACKATHON 2026", SwingConstants.LEFT);
        brandLabel.setFont(Theme.FONT_TITLE);
        brandLabel.setForeground(Theme.PRIMARY);
        brandPanel.add(brandLabel, BorderLayout.NORTH);

        JLabel roleBadge = new JLabel(role.toUpperCase() + (role.equals("Judge") ? " (" + extraInfo + ")" : ""));
        roleBadge.setFont(Theme.FONT_SMALL_BOLD);
        roleBadge.setForeground(Theme.TEXT_SECONDARY);
        brandPanel.add(roleBadge, BorderLayout.SOUTH);

        sidebarPanel.add(brandPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. User Info Widget
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBackground(Theme.PANEL_BG);
        userPanel.setMaximumSize(new Dimension(250, 60));
        userPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        JLabel welcomeLabel = new JLabel("Welcome,", SwingConstants.LEFT);
        welcomeLabel.setFont(Theme.FONT_SMALL);
        welcomeLabel.setForeground(Theme.TEXT_LIGHT);
        userPanel.add(welcomeLabel);

        JLabel nameLabel = new JLabel(userName, SwingConstants.LEFT);
        nameLabel.setFont(Theme.FONT_BODY_BOLD);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        userPanel.add(nameLabel);

        sidebarPanel.add(userPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Divider
        JPanel divider = new JPanel();
        divider.setBackground(Theme.BORDER_COLOR);
        divider.setMaximumSize(new Dimension(210, 1));
        sidebarPanel.add(divider);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 3. Navigation Buttons based on Role
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Theme.PANEL_BG);

        if ("Admin".equals(role)) {
            addMenuItem(menuPanel, "Dashboard", Theme.IconType.DASHBOARD);
            addMenuItem(menuPanel, "Teams", Theme.IconType.TEAM);
            addMenuItem(menuPanel, "Participants", Theme.IconType.PARTICIPANT);
            addMenuItem(menuPanel, "Judges", Theme.IconType.JUDGE);
            addMenuItem(menuPanel, "Projects", Theme.IconType.PROJECT);
            addMenuItem(menuPanel, "Assignments", Theme.IconType.ASSIGNMENT);
            addMenuItem(menuPanel, "Evaluations", Theme.IconType.EVALUATION);
            addMenuItem(menuPanel, "Leaderboard", Theme.IconType.LEADERBOARD);
            addMenuItem(menuPanel, "Winners", Theme.IconType.CERTIFICATE);
            addMenuItem(menuPanel, "Reports", Theme.IconType.REPORT);
        } else if ("Judge".equals(role)) {
            addMenuItem(menuPanel, "Dashboard", Theme.IconType.DASHBOARD);
            addMenuItem(menuPanel, "Evaluations", Theme.IconType.EVALUATION);
            addMenuItem(menuPanel, "Leaderboard", Theme.IconType.LEADERBOARD);
            addMenuItem(menuPanel, "Reports", Theme.IconType.REPORT);
        } else {
            // Team Leader
            addMenuItem(menuPanel, "Dashboard", Theme.IconType.DASHBOARD);
            addMenuItem(menuPanel, "My Team", Theme.IconType.TEAM);
            addMenuItem(menuPanel, "Team Members", Theme.IconType.PARTICIPANT);
            addMenuItem(menuPanel, "Project Details", Theme.IconType.PROJECT);
            addMenuItem(menuPanel, "Leaderboard", Theme.IconType.LEADERBOARD);
        }

        sidebarPanel.add(menuPanel);
        sidebarPanel.add(Box.createVerticalGlue());

        // 4. Logout Button at the bottom
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        logoutPanel.setBackground(Theme.PANEL_BG);
        logoutPanel.setMaximumSize(new Dimension(250, 80));

        CustomButton logoutBtn = new CustomButton("Logout", CustomButton.ButtonType.SECONDARY);
        logoutBtn.setIcon(Theme.getIcon(Theme.IconType.LOGOUT, 18, Theme.DANGER));
        logoutBtn.setForeground(Theme.DANGER);
        logoutBtn.setPreferredSize(new Dimension(210, 38));
        logoutBtn.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        logoutPanel.add(logoutBtn);
        sidebarPanel.add(logoutPanel);
    }

    private void addMenuItem(JPanel container, String title, Theme.IconType iconType) {
        CustomButton btn = new CustomButton(title, CustomButton.ButtonType.SECONDARY) {
            @Override
            protected void paintComponent(Graphics g) {
                // Customized menu button appearance (flat selection highlight)
                int w = getWidth();
                int h = getHeight();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (selectedMenuBtn == this) {
                    g2.setColor(Theme.PRIMARY_LIGHT);
                    g2.fillRoundRect(8, 0, w - 16, h, 6, 6);
                } else if (model.isRollover()) {
                    g2.setColor(Theme.PANEL_BG);
                    g2.fillRoundRect(8, 0, w - 16, h, 6, 6);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        // Aligning text and icons nicely
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        btn.setPreferredSize(new Dimension(250, 44));
        btn.setMaximumSize(new Dimension(250, 44));

        // State-aware Icon color loading
        Icon normalIcon = Theme.getIcon(iconType, 18, Theme.TEXT_SECONDARY);
        Icon activeIcon = Theme.getIcon(iconType, 18, Theme.PRIMARY);
        btn.setIcon(normalIcon);

        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectMenuItem(title);
            }
        });

        container.add(btn);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
        menuButtons.put(title, btn);

        // Pre-select Dashboard
        if ("Dashboard".equals(title)) {
            selectedMenuBtn = btn;
            btn.setIcon(activeIcon);
            btn.setForeground(Theme.PRIMARY);
        }
    }

    private void selectMenuItem(String title) {
        // Reset old selection
        if (selectedMenuBtn != null) {
            String oldTitle = selectedMenuBtn.getText();
            Theme.IconType iconType = getIconTypeForTitle(oldTitle);
            selectedMenuBtn.setIcon(Theme.getIcon(iconType, 18, Theme.TEXT_SECONDARY));
            selectedMenuBtn.setForeground(Theme.TEXT_SECONDARY);
        }

        // Set new selection
        CustomButton activeBtn = menuButtons.get(title);
        selectedMenuBtn = activeBtn;
        Theme.IconType iconType = getIconTypeForTitle(title);
        activeBtn.setIcon(Theme.getIcon(iconType, 18, Theme.PRIMARY));
        activeBtn.setForeground(Theme.PRIMARY);

        sidebarPanel.repaint();

        // Switch panel in CardLayout
        cardLayout.show(contentCardPanel, title);
    }

    private Theme.IconType getIconTypeForTitle(String t) {
        switch (t) {
            case "Dashboard": return Theme.IconType.DASHBOARD;
            case "Teams":
            case "My Team": return Theme.IconType.TEAM;
            case "Participants":
            case "Team Members": return Theme.IconType.PARTICIPANT;
            case "Judges": return Theme.IconType.JUDGE;
            case "Projects":
            case "Project Details": return Theme.IconType.PROJECT;
            case "Assignments": return Theme.IconType.ASSIGNMENT;
            case "Evaluations": return Theme.IconType.EVALUATION;
            case "Leaderboard": return Theme.IconType.LEADERBOARD;
            case "Winners": return Theme.IconType.CERTIFICATE;
            case "Reports": return Theme.IconType.REPORT;
            default: return Theme.IconType.DASHBOARD;
        }
    }

    private void setupContentPanel() {
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setBackground(Color.WHITE);

        // 1. main Dashboard panel
        JPanel mainStatsPanel = createMainStatsPanel();
        contentCardPanel.add(mainStatsPanel, "Dashboard");

        // 2. Add other Panels depending on role
        if ("Admin".equals(role)) {
            contentCardPanel.add(new TeamPanel(role, userId), "Teams");
            contentCardPanel.add(new ParticipantPanel(role, userId), "Participants");
            contentCardPanel.add(new JudgePanel(), "Judges");
            contentCardPanel.add(new ProjectPanel(role, userId), "Projects");
            contentCardPanel.add(new AssignmentPanel(), "Assignments");
            contentCardPanel.add(new EvaluationPanel(role, userId, userName), "Evaluations");
            contentCardPanel.add(new LeaderboardPanel(), "Leaderboard");
            contentCardPanel.add(new CertificatePanel(), "Winners");
            contentCardPanel.add(new ReportsPanel(), "Reports");
        } else if ("Judge".equals(role)) {
            contentCardPanel.add(new EvaluationPanel(role, userId, userName), "Evaluations");
            contentCardPanel.add(new LeaderboardPanel(), "Leaderboard");
            contentCardPanel.add(new ReportsPanel(userId), "Reports");
        } else {
            // Team Leader
            contentCardPanel.add(new TeamPanel(role, userId), "My Team");
            contentCardPanel.add(new ParticipantPanel(role, userId), "Team Members");
            contentCardPanel.add(new ProjectPanel(role, userId), "Project Details");
            contentCardPanel.add(new LeaderboardPanel(), "Leaderboard");
        }
    }

    private JPanel createMainStatsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Top Greeting Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Overview Dashboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("College Hackathon 2026 Management Console");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);
        headerPanel.add(sub, BorderLayout.SOUTH);

        wrapper.add(headerPanel, BorderLayout.NORTH);

        // Stats Cards Panel
        JPanel cardsGrid = new JPanel(new GridLayout(1, 5, 15, 0));
        cardsGrid.setBackground(Color.WHITE);
        cardsGrid.setPreferredSize(new Dimension(800, 110));

        // Fetch values from DB
        int teamsCount = 0;
        int participantsCount = 0;
        int projectsCount = 0;
        int judgesCount = 0;
        int completedEvaluations = 0;

        if ("Admin".equals(role)) {
            try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM teams");
                if (rs.next()) teamsCount = rs.getInt(1);
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM participants");
                if (rs.next()) participantsCount = rs.getInt(1);
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM projects");
                if (rs.next()) projectsCount = rs.getInt(1);
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM judges");
                if (rs.next()) judgesCount = rs.getInt(1);
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM evaluations");
                if (rs.next()) completedEvaluations = rs.getInt(1);
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            cardsGrid.add(createCard("Total Teams", String.valueOf(teamsCount), Theme.IconType.TEAM, Theme.PRIMARY));
            cardsGrid.add(createCard("Participants", String.valueOf(participantsCount), Theme.IconType.PARTICIPANT, Theme.ACCENT));
            cardsGrid.add(createCard("Projects Submitted", String.valueOf(projectsCount), Theme.IconType.PROJECT, Theme.SUCCESS));
            cardsGrid.add(createCard("Total Judges", String.valueOf(judgesCount), Theme.IconType.JUDGE, Theme.WARNING));
            cardsGrid.add(createCard("Evaluations Done", String.valueOf(completedEvaluations), Theme.IconType.EVALUATION, Theme.DANGER));
        } else if ("Judge".equals(role)) {
            int assigned = 0;
            int graded = 0;
            double avgScore = 0.0;
            try (Connection conn = DatabaseHelper.getConnection()) {
                String q1 = "SELECT COUNT(*) FROM assignments WHERE judge_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(q1)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) assigned = rs.getInt(1);
                    }
                }
                String q2 = "SELECT COUNT(*), AVG(total_score) FROM evaluations WHERE judge_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(q2)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            graded = rs.getInt(1);
                            avgScore = rs.getDouble(2);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            cardsGrid.add(createCard("Assigned Projects", String.valueOf(assigned), Theme.IconType.PROJECT, Theme.PRIMARY));
            cardsGrid.add(createCard("Evaluations Done", String.valueOf(graded), Theme.IconType.EVALUATION, Theme.SUCCESS));
            cardsGrid.add(createCard("Pending Projects", String.valueOf(assigned - graded), Theme.IconType.ASSIGNMENT, Theme.WARNING));
            cardsGrid.add(createCard("Avg Score Given", String.format("%.1f", avgScore), Theme.IconType.LEADERBOARD, Theme.ACCENT));
            // filler card
            cardsGrid.add(createCard("Active Judges", "4", Theme.IconType.JUDGE, Theme.DANGER));
        } else {
            // Team Leader
            int members = 0;
            String projTitle = "Not Submitted";
            int rank = 99;
            try (Connection conn = DatabaseHelper.getConnection()) {
                String q1 = "SELECT COUNT(*) FROM participants WHERE team_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(q1)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) members = rs.getInt(1);
                    }
                }
                String q2 = "SELECT title FROM projects WHERE team_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(q2)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) projTitle = rs.getString("title");
                    }
                }
                // Calculate Rank
                String rankQ = "SELECT team_id, AVG(total_score) as score FROM evaluations e " +
                               "JOIN projects p ON p.project_id = e.project_id " +
                               "GROUP BY team_id ORDER BY score DESC";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(rankQ)) {
                    int rIdx = 1;
                    while (rs.next()) {
                        if (rs.getInt("team_id") == userId) {
                            rank = rIdx;
                            break;
                        }
                        rIdx++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            cardsGrid.add(createCard("My Team ID", String.valueOf(userId), Theme.IconType.TEAM, Theme.PRIMARY));
            cardsGrid.add(createCard("Members", String.valueOf(members), Theme.IconType.PARTICIPANT, Theme.ACCENT));
            cardsGrid.add(createCard("Project Upload", projTitle.equals("Not Submitted") ? "Pending" : "Completed", Theme.IconType.PROJECT, projTitle.equals("Not Submitted") ? Theme.DANGER : Theme.SUCCESS));
            cardsGrid.add(createCard("Leaderboard Rank", rank == 99 ? "N/A" : "#" + rank, Theme.IconType.LEADERBOARD, Theme.WARNING));
            // filler
            cardsGrid.add(createCard("Hackathon Phase", "Evaluation", Theme.IconType.ASSIGNMENT, Theme.DANGER));
        }

        wrapper.add(cardsGrid, BorderLayout.CENTER);

        // Graphics2D Analytics charts panel
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setBackground(Color.WHITE);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        chartsPanel.setPreferredSize(new Dimension(800, 360));

        AnalyticsChart donut = new AnalyticsChart(AnalyticsChart.ChartType.DONUT, "Projects by Domain Category");
        AnalyticsChart bar = new AnalyticsChart(AnalyticsChart.ChartType.BAR, "Average Evaluation Score by Domain");

        // Load DB data into charts
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement()) {
            // Domain distribution donut
            ResultSet rs = stmt.executeQuery("SELECT domain, COUNT(*) FROM projects GROUP BY domain");
            while (rs.next()) {
                donut.addData(rs.getString(1), rs.getDouble(2));
            }
            rs.close();

            // Average score bar chart
            String barQuery = "SELECT p.domain, AVG(e.total_score) " +
                              "FROM evaluations e " +
                              "JOIN projects p ON p.project_id = e.project_id " +
                              "GROUP BY p.domain";
            rs = stmt.executeQuery(barQuery);
            while (rs.next()) {
                bar.addData(rs.getString(1), rs.getDouble(2));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        chartsPanel.add(donut);
        chartsPanel.add(bar);

        wrapper.add(chartsPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private RoundedPanel createCard(String titleText, String countText, Theme.IconType iconType, Color accentColor) {
        // Subtle border rounded panel card
        RoundedPanel card = new RoundedPanel(10, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
        card.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.weightx = 1.0;
        gbc.gridy = 0;

        // Top Column: Title
        JLabel title = new JLabel(titleText);
        title.setFont(Theme.FONT_SMALL_BOLD);
        title.setForeground(Theme.TEXT_SECONDARY);
        gbc.gridx = 0;
        gbc.weighty = 0.3;
        card.add(title, gbc);

        // Icon on Right of title column
        JLabel iconLabel = new JLabel(Theme.getIcon(iconType, 24, accentColor));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        card.add(iconLabel, gbc);

        // Row 2: Large Count
        JLabel count = new JLabel(countText);
        count.setFont(Theme.FONT_TITLE);
        count.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 0.7;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        card.add(count, gbc);

        return card;
    }
}
