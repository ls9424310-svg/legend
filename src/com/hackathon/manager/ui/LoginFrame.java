package com.hackathon.manager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.hackathon.manager.db.DatabaseHelper;
import com.hackathon.manager.ui.components.CustomButton;
import com.hackathon.manager.ui.components.CustomPasswordField;
import com.hackathon.manager.ui.components.CustomTextField;

public class LoginFrame extends JFrame {
    private JComboBox<String> roleCombo;
    private CustomTextField userField;
    private CustomPasswordField passField;
    private CustomButton loginBtn;
    private CustomButton exitBtn;

    public LoginFrame() {
        super("Hackathon Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // 1. Left Panel - Banner Graphic
        JPanel leftPanel = new JPanel() {
            private Image bgImg;
            {
                try {
                    URL imgUrl = getClass().getResource("login_banner.png");
                    if (imgUrl != null) {
                        bgImg = new ImageIcon(imgUrl).getImage();
                    }
                } catch (Exception e) {
                    System.err.println("Banner image not found: " + e.getMessage());
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (bgImg != null) {
                    // Draw image keeping ratio
                    int imgW = bgImg.getWidth(null);
                    int imgH = bgImg.getHeight(null);
                    double ratio = Math.min((double) getWidth() / imgW, (double) getHeight() / imgH);
                    int w = (int) (imgW * ratio);
                    int h = (int) (imgH * ratio);
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.drawImage(bgImg, x, y, w, h, null);
                } else {
                    // Fallback visual panel
                    g2.setColor(Theme.PRIMARY);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.WHITE);
                    g2.setFont(Theme.FONT_TITLE);
                    g2.drawString("HACKATHON 2026", 40, 150);
                    g2.setFont(Theme.FONT_BODY);
                    g2.drawString("Innovate. Code. Deploy.", 40, 190);
                }
                g2.dispose();
            }
        };
        leftPanel.setPreferredSize(new Dimension(380, 480));
        leftPanel.setBackground(Color.WHITE);

        // 2. Right Panel - Form Controls
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        // Title
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 0;
        rightPanel.add(titleLabel, gbc);

        // Subtitle
        JLabel subLabel = new JLabel("Sign in to continue");
        subLabel.setFont(Theme.FONT_BODY);
        subLabel.setForeground(Theme.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(subLabel, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);

        // Role Selector Label
        JLabel roleLabel = new JLabel("Choose Role");
        roleLabel.setFont(Theme.FONT_BODY_BOLD);
        roleLabel.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 2;
        rightPanel.add(roleLabel, gbc);

        // Role Selector ComboBox
        String[] roles = {"Admin", "Team Leader", "Judge"};
        roleCombo = new JComboBox<>(roles);
        roleCombo.setFont(Theme.FONT_BODY);
        roleCombo.setBackground(Color.WHITE);
        roleCombo.setPreferredSize(new Dimension(280, 38));
        gbc.gridy = 3;
        rightPanel.add(roleCombo, gbc);

        // Username Label
        JLabel userLabel = new JLabel("Username / Email");
        userLabel.setFont(Theme.FONT_BODY_BOLD);
        userLabel.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 4;
        rightPanel.add(userLabel, gbc);

        // Username Field
        userField = new CustomTextField("Enter username or email");
        userField.setPreferredSize(new Dimension(280, 38));
        gbc.gridy = 5;
        rightPanel.add(userField, gbc);

        // Password Label
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_BODY_BOLD);
        passLabel.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 6;
        rightPanel.add(passLabel, gbc);

        // Password Field
        passField = new CustomPasswordField("Enter your password");
        passField.setPreferredSize(new Dimension(280, 38));
        gbc.gridy = 7;
        rightPanel.add(passField, gbc);

        // Action Buttons Panel
        JPanel btnPanel = new JPanel(new java.awt.GridLayout(1, 2, 12, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setPreferredSize(new Dimension(280, 38));

        exitBtn = new CustomButton("Exit", CustomButton.ButtonType.SECONDARY);
        exitBtn.addActionListener(e -> System.exit(0));
        btnPanel.add(exitBtn);

        loginBtn = new CustomButton("Login", CustomButton.ButtonType.PRIMARY);
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        btnPanel.add(loginBtn);

        gbc.gridy = 8;
        gbc.insets = new Insets(20, 0, 0, 0);
        rightPanel.add(btnPanel, gbc);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void handleLogin() {
        String role = (String) roleCombo.getSelectedItem();
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both credentials.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            if ("Admin".equals(role)) {
                String query = "SELECT * FROM admins WHERE username = ? AND password = ?";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, username);
                    ps.setString(2, password);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Launch Admin Dashboard
                            DashboardFrame dash = new DashboardFrame("Admin", 0, "System Administrator", "");
                            dash.setVisible(true);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid Admin Credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else if ("Judge".equals(role)) {
                String query = "SELECT judge_id, name, domain, password FROM judges WHERE email = ?";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && password.equals(rs.getString("password"))) {
                            int judgeId = rs.getInt("judge_id");
                            String judgeName = rs.getString("name");
                            String domain = rs.getString("domain");
                            
                            // Launch Judge Dashboard
                            DashboardFrame dash = new DashboardFrame("Judge", judgeId, judgeName, domain);
                            dash.setVisible(true);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid Judge Credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else {
                // Team Leader
                String query = "SELECT team_id, team_name, leader_name FROM teams WHERE email = ?";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        // Check if exists and default password "password" matches
                        if (rs.next() && "password".equals(password)) {
                            int teamId = rs.getInt("team_id");
                            String leaderName = rs.getString("leader_name");
                            
                            // Launch Team Leader Dashboard
                            DashboardFrame dash = new DashboardFrame("Team Leader", teamId, leaderName, "");
                            dash.setVisible(true);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid Team Leader Credentials.\nNote: Registered Team Email is username and 'password' is password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Connection Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
