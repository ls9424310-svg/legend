package com.hackathon.manager.util;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import com.hackathon.manager.db.DatabaseHelper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PDFReportGenerator {

    // Style Helper Fonts
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.BOLD, new java.awt.Color(37, 99, 235));
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, new java.awt.Color(17, 24, 39));
    private static final Font FONT_BODY_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, new java.awt.Color(17, 24, 39));
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, new java.awt.Color(75, 85, 99));
    private static final Font FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, new java.awt.Color(156, 163, 175));

    // Colors
    private static final java.awt.Color HEADER_BG = new java.awt.Color(37, 99, 235); // Royal Blue
    private static final java.awt.Color ROW_BG_ALT = new java.awt.Color(249, 250, 251);

    /**
     * Generates a PDF certificate for Winner/Participation/Judge
     */
    public static void generateCertificate(String destPath, String recipientName, String type, String details) throws Exception {
        // Landscape document
        Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(destPath));
        document.open();

        // 1. Drawing a border (Outer Table)
        PdfPTable borderTable = new PdfPTable(1);
        borderTable.setWidthPercentage(100);
        PdfPCell borderCell = new PdfPCell();
        borderCell.setBorder(Rectangle.BOX);
        borderCell.setBorderWidth(5);
        borderCell.setBorderColor(HEADER_BG);
        borderCell.setPadding(35);

        // Certificate content
        Paragraph pSpace = new Paragraph(" ");
        pSpace.setSpacingAfter(15);

        Font certTitleFont = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 32, Font.BOLD, HEADER_BG);
        Paragraph pTitle = new Paragraph("HACKATHON 2026", certTitleFont);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        borderCell.addElement(pTitle);
        borderCell.addElement(pSpace);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.NORMAL, new java.awt.Color(75, 85, 99));
        Paragraph pSub = new Paragraph("CERTIFICATE OF " + type.toUpperCase(), subtitleFont);
        pSub.setAlignment(Element.ALIGN_CENTER);
        borderCell.addElement(pSub);
        borderCell.addElement(pSpace);

        Paragraph pPresented = new Paragraph("This is proudly presented to", FontFactory.getFont(FontFactory.HELVETICA, 12, Font.ITALIC));
        pPresented.setAlignment(Element.ALIGN_CENTER);
        borderCell.addElement(pPresented);
        borderCell.addElement(pSpace);

        Font nameFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 26, Font.BOLD, new java.awt.Color(17, 24, 39));
        Paragraph pName = new Paragraph(recipientName, nameFont);
        pName.setAlignment(Element.ALIGN_CENTER);
        borderCell.addElement(pName);
        borderCell.addElement(pSpace);

        Font detailFont = FontFactory.getFont(FontFactory.HELVETICA, 13, Font.NORMAL, new java.awt.Color(17, 24, 39));
        Paragraph pDetails = new Paragraph(details, detailFont);
        pDetails.setAlignment(Element.ALIGN_CENTER);
        borderCell.addElement(pDetails);
        borderCell.addElement(pSpace);

        // Verification QR code at the bottom right
        String verificationText = "Verified Certificate\nRecipient: " + recipientName + "\nType: " + type + "\nDate: " + new Date();
        BufferedImage qrImg = QRCodeGenerator.generateQRCodeImage(verificationText, 100, 100);
        
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        footerTable.setWidths(new float[]{3.0f, 1.0f});

        // Signatures on Left
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        Paragraph pSigs = new Paragraph("\n\n_______________________\nOrganizing Chairman", FONT_BODY_BOLD);
        pSigs.setAlignment(Element.ALIGN_LEFT);
        leftCell.addElement(pSigs);
        footerTable.addCell(leftCell);

        // QR Code on Right
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (qrImg != null) {
            Image img = Image.getInstance(qrImg, null);
            img.setAlignment(Element.ALIGN_RIGHT);
            img.scaleAbsolute(70, 70);
            rightCell.addElement(img);
            Paragraph qrLabel = new Paragraph("Scan to Verify", FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL));
            qrLabel.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(qrLabel);
        }
        footerTable.addCell(rightCell);

        borderCell.addElement(footerTable);
        borderTable.addCell(borderCell);
        document.add(borderTable);
        document.close();
    }

    /**
     * Generates a PDF Report of Teams and Members
     */
    public static void generateTeamReport(String destPath) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter.getInstance(document, new FileOutputStream(destPath));
        document.open();

        document.add(new Paragraph("Hackathon Management System", FONT_FOOTER));
        Paragraph title = new Paragraph("Hackathon Teams & Members Report", FONT_TITLE);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 2.0f, 2.0f, 3.0f, 2.5f});

        // Table Header
        addTableHeaderCell(table, "ID");
        addTableHeaderCell(table, "Team Name");
        addTableHeaderCell(table, "Leader Name");
        addTableHeaderCell(table, "College Name");
        addTableHeaderCell(table, "Email / Phone");

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM teams")) {
            
            int rowIdx = 0;
            while (rs.next()) {
                java.awt.Color bg = (rowIdx++ % 2 == 0) ? java.awt.Color.WHITE : ROW_BG_ALT;
                
                int teamId = rs.getInt("team_id");
                addTableCell(table, String.valueOf(teamId), bg);
                addTableCell(table, rs.getString("team_name"), bg);
                addTableCell(table, rs.getString("leader_name"), bg);
                addTableCell(table, rs.getString("college"), bg);
                addTableCell(table, rs.getString("email") + "\n" + rs.getString("phone"), bg);

                // Fetch participants for this team and print inline or nested
                PdfPCell memberCell = new PdfPCell();
                memberCell.setColspan(5);
                memberCell.setBackgroundColor(bg);
                memberCell.setPaddingLeft(30);
                memberCell.setPaddingBottom(10);
                memberCell.setPaddingTop(5);
                memberCell.setBorder(Rectangle.BOTTOM);
                memberCell.setBorderColor(new java.awt.Color(229, 231, 235));

                Paragraph pMemHead = new Paragraph("Team Members:", FONT_BODY_BOLD);
                memberCell.addElement(pMemHead);

                String memQuery = "SELECT name, branch, semester, email FROM participants WHERE team_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(memQuery)) {
                    ps.setInt(1, teamId);
                    try (ResultSet rsMem = ps.executeQuery()) {
                        while (rsMem.next()) {
                            String mStr = "• " + rsMem.getString("name") + " (" + rsMem.getString("branch") + 
                                          ", Sem " + rsMem.getString("semester") + ") - " + rsMem.getString("email");
                            memberCell.addElement(new Paragraph(mStr, FONT_BODY));
                        }
                    }
                }
                table.addCell(memberCell);
            }
        }

        document.add(table);
        document.close();
    }

    /**
     * Generates a PDF Report of Judges and Assigned Evaluations
     */
    public static void generateJudgeReport(String destPath) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter.getInstance(document, new FileOutputStream(destPath));
        document.open();

        document.add(new Paragraph("Hackathon Management System", FONT_FOOTER));
        Paragraph title = new Paragraph("Hackathon Judge Assignments & Scores", FONT_TITLE);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 2.5f, 2.0f, 4.5f});

        addTableHeaderCell(table, "ID");
        addTableHeaderCell(table, "Judge Name");
        addTableHeaderCell(table, "Expertise");
        addTableHeaderCell(table, "Assigned Projects & Scores Given");

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM judges")) {

            int rowIdx = 0;
            while (rs.next()) {
                java.awt.Color bg = (rowIdx++ % 2 == 0) ? java.awt.Color.WHITE : ROW_BG_ALT;
                int judgeId = rs.getInt("judge_id");
                
                addTableCell(table, String.valueOf(judgeId), bg);
                addTableCell(table, rs.getString("name"), bg);
                addTableCell(table, rs.getString("domain"), bg);

                // Fetch assignments and evaluations for this judge
                PdfPCell assignCell = new PdfPCell();
                assignCell.setBackgroundColor(bg);
                assignCell.setPadding(8);
                assignCell.setBorder(Rectangle.BOX);
                assignCell.setBorderColor(new java.awt.Color(229, 231, 235));

                String query = "SELECT p.title, t.team_name, e.total_score " +
                               "FROM assignments a " +
                               "JOIN projects p ON p.project_id = a.project_id " +
                               "JOIN teams t ON t.team_id = p.team_id " +
                               "LEFT JOIN evaluations e ON e.project_id = a.project_id AND e.judge_id = a.judge_id " +
                               "WHERE a.judge_id = ?";
                
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setInt(1, judgeId);
                    try (ResultSet rsAssign = ps.executeQuery()) {
                        boolean hasAssignments = false;
                        while (rsAssign.next()) {
                            hasAssignments = true;
                            String score = rsAssign.getObject("total_score") != null ? 
                                           rsAssign.getString("total_score") + "/50" : "Pending Evaluation";
                            String text = "• Project: " + rsAssign.getString("title") + 
                                          " [Team: " + rsAssign.getString("team_name") + "] -> Score: " + score;
                            assignCell.addElement(new Paragraph(text, FONT_BODY));
                        }
                        if (!hasAssignments) {
                            assignCell.addElement(new Paragraph("No projects assigned.", FONT_BODY));
                        }
                    }
                }
                table.addCell(assignCell);
            }
        }

        document.add(table);
        document.close();
    }

    /**
     * Generates a PDF Report of Projects Grouped by Domain
     */
    public static void generateProjectReport(String destPath) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter.getInstance(document, new FileOutputStream(destPath));
        document.open();

        document.add(new Paragraph("Hackathon Management System", FONT_FOOTER));
        Paragraph title = new Paragraph("Hackathon Domain-wise Project Report", FONT_TITLE);
        title.setSpacingAfter(20);
        document.add(title);

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT p.*, t.team_name FROM projects p JOIN teams t ON t.team_id = p.team_id ORDER BY p.domain")) {

            String currentDomain = "";
            PdfPTable table = null;

            while (rs.next()) {
                String domain = rs.getString("domain");
                if (!domain.equals(currentDomain)) {
                    if (table != null) {
                        document.add(table);
                        document.add(new Paragraph("\n"));
                    }
                    currentDomain = domain;
                    Paragraph domHeader = new Paragraph("Domain: " + currentDomain, FONT_HEADER);
                    domHeader.setSpacingAfter(10);
                    document.add(domHeader);

                    table = new PdfPTable(4);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{1.5f, 3.5f, 2.5f, 2.5f});
                    addTableHeaderCell(table, "Team Name");
                    addTableHeaderCell(table, "Project Title");
                    addTableHeaderCell(table, "GitHub Repository");
                    addTableHeaderCell(table, "PPT Presentation");
                }

                java.awt.Color bg = java.awt.Color.WHITE;
                addTableCell(table, rs.getString("team_name"), bg);
                addTableCell(table, rs.getString("title"), bg);
                addTableCell(table, rs.getString("github_link"), bg);
                addTableCell(table, rs.getString("ppt_link"), bg);
            }

            if (table != null) {
                document.add(table);
            }
        }

        document.close();
    }

    /**
     * Generates a PDF Report of Winners (Top scoring projects)
     */
    public static void generateWinnerReport(String destPath) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter.getInstance(document, new FileOutputStream(destPath));
        document.open();

        document.add(new Paragraph("Hackathon Management System", FONT_FOOTER));
        Paragraph title = new Paragraph("Hackathon Winner & Leaderboard Report", FONT_TITLE);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 2.5f, 3.0f, 2.5f, 1.5f});

        addTableHeaderCell(table, "Rank");
        addTableHeaderCell(table, "Team Name");
        addTableHeaderCell(table, "Project Title");
        addTableHeaderCell(table, "College Name");
        addTableHeaderCell(table, "Avg Score");

        String query = "SELECT t.team_name, t.college, p.title, AVG(e.total_score) as avg_score " +
                       "FROM evaluations e " +
                       "JOIN projects p ON p.project_id = e.project_id " +
                       "JOIN teams t ON t.team_id = p.team_id " +
                       "GROUP BY t.team_id " +
                       "ORDER BY avg_score DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int rank = 1;
            while (rs.next()) {
                java.awt.Color bg;
                if (rank == 1) {
                    bg = new java.awt.Color(254, 243, 199); // Gold bg for 1st
                } else if (rank == 2) {
                    bg = new java.awt.Color(243, 244, 246); // Silver bg for 2nd
                } else if (rank == 3) {
                    bg = new java.awt.Color(255, 237, 213); // Bronze bg for 3rd
                } else {
                    bg = (rank % 2 == 0) ? ROW_BG_ALT : java.awt.Color.WHITE;
                }

                addTableCell(table, String.valueOf(rank), bg);
                addTableCell(table, rs.getString("team_name"), bg);
                addTableCell(table, rs.getString("title"), bg);
                addTableCell(table, rs.getString("college"), bg);
                addTableCell(table, String.format("%.2f", rs.getDouble("avg_score")), bg);

                rank++;
            }
        }

        document.add(table);
        document.close();
    }

    private static void addTableHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, java.awt.Color.WHITE)));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private static void addTableCell(PdfPTable table, String text, java.awt.Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", FONT_BODY));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new java.awt.Color(229, 231, 235));
        table.addCell(cell);
    }
}
