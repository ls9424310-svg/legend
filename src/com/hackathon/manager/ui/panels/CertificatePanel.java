package com.hackathon.manager.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.hackathon.manager.db.DatabaseHelper;
import com.hackathon.manager.ui.Theme;
import com.hackathon.manager.ui.components.CustomButton;
import com.hackathon.manager.ui.components.CustomTextField;
import com.hackathon.manager.ui.components.RoundedPanel;
import com.hackathon.manager.util.PDFReportGenerator;

public class CertificatePanel extends JPanel {
    private JComboBox<String> typeCombo;
    private JComboBox<String> recipientCombo;
    private JComboBox<String> awardCategoryCombo;
    private CustomTextField txtCollegeOrDomain;
    private JTextArea txtCustomDetails;
    private CustomButton btnGenerate;

    private final List<String> recipientNames = new ArrayList<>();
    private JPanel awardRowPanel;

    public CertificatePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel title = new JLabel("Certificate Generator");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(title, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        setupUI();
    }

    private void setupUI() {
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        centerWrapper.setBackground(Color.WHITE);

        RoundedPanel formPanel = new RoundedPanel(10, Theme.PANEL_BG, true, Theme.BORDER_COLOR);
        formPanel.setPreferredSize(new Dimension(500, 520));
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        JLabel welcomeLabel = new JLabel("Issue Certificate of Excellence", JLabel.CENTER);
        welcomeLabel.setFont(Theme.FONT_SUBTITLE);
        welcomeLabel.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(welcomeLabel, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);

        // Certificate Type Selector
        JLabel lblType = new JLabel("Certificate Type");
        lblType.setFont(Theme.FONT_BODY_BOLD);
        lblType.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 1;
        formPanel.add(lblType, gbc);

        String[] types = {"Winner", "Participation", "Judge Appreciation"};
        typeCombo = new JComboBox<>(types);
        typeCombo.setFont(Theme.FONT_BODY);
        typeCombo.setBackground(Color.WHITE);
        typeCombo.setPreferredSize(new Dimension(400, 36));
        gbc.gridy = 2;
        formPanel.add(typeCombo, gbc);

        // Recipient Selector
        JLabel lblRecipient = new JLabel("Recipient Name");
        lblRecipient.setFont(Theme.FONT_BODY_BOLD);
        lblRecipient.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 3;
        formPanel.add(lblRecipient, gbc);

        recipientCombo = new JComboBox<>();
        recipientCombo.setFont(Theme.FONT_BODY);
        recipientCombo.setBackground(Color.WHITE);
        recipientCombo.setPreferredSize(new Dimension(400, 36));
        gbc.gridy = 4;
        formPanel.add(recipientCombo, gbc);

        // Award Category (visible for Winner type only)
        awardRowPanel = new JPanel(new BorderLayout());
        awardRowPanel.setBackground(Theme.PANEL_BG);
        JLabel lblAward = new JLabel("Award Category");
        lblAward.setFont(Theme.FONT_BODY_BOLD);
        lblAward.setForeground(Theme.TEXT_PRIMARY);
        awardRowPanel.add(lblAward, BorderLayout.NORTH);

        String[] awards = {
            "1st Position Overall", "2nd Position Overall", "3rd Position Overall",
            "Best Innovation Award", "Best UI/UX Design Award", "Best Social Impact Award"
        };
        awardCategoryCombo = new JComboBox<>(awards);
        awardCategoryCombo.setFont(Theme.FONT_BODY);
        awardCategoryCombo.setBackground(Color.WHITE);
        awardCategoryCombo.setPreferredSize(new Dimension(400, 36));
        awardRowPanel.add(awardCategoryCombo, BorderLayout.SOUTH);
        gbc.gridy = 5;
        formPanel.add(awardRowPanel, gbc);

        // College or Domain Info
        JLabel lblInfo = new JLabel("College / Department / Domain");
        lblInfo.setFont(Theme.FONT_BODY_BOLD);
        lblInfo.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 6;
        formPanel.add(lblInfo, gbc);

        txtCollegeOrDomain = new CustomTextField("Enter college name or expertise area");
        txtCollegeOrDomain.setPreferredSize(new Dimension(400, 36));
        gbc.gridy = 7;
        formPanel.add(txtCollegeOrDomain, gbc);

        // Custom Details text area
        JLabel lblDetails = new JLabel("Additional Certificate Subtext");
        lblDetails.setFont(Theme.FONT_BODY_BOLD);
        lblDetails.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridy = 8;
        formPanel.add(lblDetails, gbc);

        txtCustomDetails = new JTextArea(3, 20);
        txtCustomDetails.setFont(Theme.FONT_BODY);
        txtCustomDetails.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        txtCustomDetails.setLineWrap(true);
        txtCustomDetails.setWrapStyleWord(true);
        gbc.gridy = 9;
        formPanel.add(new JScrollPane(txtCustomDetails), gbc);

        // Generate Button
        btnGenerate = new CustomButton("Generate PDF Certificate", CustomButton.ButtonType.PRIMARY);
        btnGenerate.setPreferredSize(new Dimension(220, 38));
        gbc.gridy = 10;
        gbc.insets = new Insets(15, 0, 0, 0);
        formPanel.add(btnGenerate, gbc);

        centerWrapper.add(formPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // Initialize lists
        onTypeChange();

        // Listeners
        typeCombo.addActionListener(e -> onTypeChange());
        recipientCombo.addActionListener(e -> updateDetailsPlaceholder());
        btnGenerate.addActionListener(e -> generateCertificatePDF());
    }

    private void onTypeChange() {
        String type = (String) typeCombo.getSelectedItem();
        boolean isWinner = "Winner".equals(type);
        awardRowPanel.setVisible(isWinner);
        
        loadRecipients(type);
        updateDetailsPlaceholder();
    }

    private void loadRecipients(String certType) {
        recipientCombo.removeAllItems();
        recipientNames.clear();

        String query;
        if ("Judge Appreciation".equals(certType)) {
            query = "SELECT name, domain FROM judges";
        } else {
            // Participation or Winner goes to teams
            query = "SELECT team_name, college FROM teams";
        }

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String name = rs.getString(1);
                String detail = rs.getString(2);
                recipientCombo.addItem(name + " (" + detail + ")");
                recipientNames.add(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDetailsPlaceholder() {
        String type = (String) typeCombo.getSelectedItem();
        int selIdx = recipientCombo.getSelectedIndex();
        if (selIdx == -1 || selIdx >= recipientNames.size()) return;

        String name = recipientNames.get(selIdx);

        if ("Winner".equals(type)) {
            String category = (String) awardCategoryCombo.getSelectedItem();
            txtCustomDetails.setText("For outstanding performance and securing the " + category + 
                                     "\nin the College Hackathon 2026 for their project submission.");
            txtCollegeOrDomain.setText("IIT Bombay"); // default sample
        } else if ("Participation".equals(type)) {
            txtCustomDetails.setText("For active participation and displaying exceptional coding skills\nin the College Hackathon 2026.");
            txtCollegeOrDomain.setText("IIT Bombay");
        } else {
            // Judge Appreciation
            txtCustomDetails.setText("For their valuable time, support, and contribution as an expert Judge\nin evaluation of student project submissions.");
            txtCollegeOrDomain.setText("Expert Panelist");
        }
    }

    private void generateCertificatePDF() {
        int selIdx = recipientCombo.getSelectedIndex();
        if (selIdx == -1) {
            JOptionPane.showMessageDialog(this, "No recipient selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String recipientName = recipientNames.get(selIdx);
        String type = (String) typeCombo.getSelectedItem();
        String details = txtCustomDetails.getText().trim();
        String info = txtCollegeOrDomain.getText().trim();

        String fullDetailsText = details + "\nPresented on 7th July 2026 | College Campus, Bengaluru";

        // Save File Dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Certificate PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        fileChooser.setSelectedFile(new File(recipientName.toLowerCase().replace(" ", "_") + "_certificate.pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String dest = fileToSave.getAbsolutePath();
            if (!dest.toLowerCase().endsWith(".pdf")) {
                dest += ".pdf";
            }

            try {
                PDFReportGenerator.generateCertificate(dest, recipientName, type, fullDetailsText);
                JOptionPane.showMessageDialog(this, "Certificate generated successfully!\nSaved to: " + dest, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to generate certificate: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}
