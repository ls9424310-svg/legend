package com.hackathon.manager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:hackathon.db";

    static {
        try {
            // Force load SQLite JDBC Driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Teams table
            stmt.execute("CREATE TABLE IF NOT EXISTS teams (" +
                    "team_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "team_name TEXT NOT NULL," +
                    "leader_name TEXT NOT NULL," +
                    "college TEXT NOT NULL," +
                    "phone TEXT," +
                    "email TEXT" +
                    ");");

            // 2. Participants table
            stmt.execute("CREATE TABLE IF NOT EXISTS participants (" +
                    "participant_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "team_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "branch TEXT," +
                    "semester TEXT," +
                    "email TEXT," +
                    "FOREIGN KEY(team_id) REFERENCES teams(team_id) ON DELETE CASCADE" +
                    ");");

            // 3. Judges table (added password field for login)
            stmt.execute("CREATE TABLE IF NOT EXISTS judges (" +
                    "judge_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "domain TEXT NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "phone TEXT," +
                    "password TEXT DEFAULT 'password'" +
                    ");");

            // 4. Projects table
            stmt.execute("CREATE TABLE IF NOT EXISTS projects (" +
                    "project_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "team_id INTEGER UNIQUE NOT NULL," +
                    "title TEXT NOT NULL," +
                    "domain TEXT NOT NULL," +
                    "description TEXT," +
                    "github_link TEXT," +
                    "ppt_link TEXT," +
                    "FOREIGN KEY(team_id) REFERENCES teams(team_id) ON DELETE CASCADE" +
                    ");");

            // 5. Assignments table
            stmt.execute("CREATE TABLE IF NOT EXISTS assignments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "project_id INTEGER NOT NULL," +
                    "judge_id INTEGER NOT NULL," +
                    "UNIQUE(project_id, judge_id)," +
                    "FOREIGN KEY(project_id) REFERENCES projects(project_id) ON DELETE CASCADE," +
                    "FOREIGN KEY(judge_id) REFERENCES judges(judge_id) ON DELETE CASCADE" +
                    ");");

            // 6. Evaluations table
            stmt.execute("CREATE TABLE IF NOT EXISTS evaluations (" +
                    "evaluation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "project_id INTEGER NOT NULL," +
                    "judge_id INTEGER NOT NULL," +
                    "innovation INTEGER DEFAULT 0," +
                    "technical INTEGER DEFAULT 0," +
                    "uiux INTEGER DEFAULT 0," +
                    "presentation INTEGER DEFAULT 0," +
                    "impact INTEGER DEFAULT 0," +
                    "total_score INTEGER DEFAULT 0," +
                    "UNIQUE(project_id, judge_id)," +
                    "FOREIGN KEY(project_id) REFERENCES projects(project_id) ON DELETE CASCADE," +
                    "FOREIGN KEY(judge_id) REFERENCES judges(judge_id) ON DELETE CASCADE" +
                    ");");

            // 7. Admins table
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                    "username TEXT PRIMARY KEY," +
                    "password TEXT NOT NULL," +
                    "name TEXT NOT NULL" +
                    ");");

            // Seed Admins
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admins");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO admins (username, password, name) VALUES ('admin', 'admin', 'System Administrator');");
            }
            rs.close();

            // Seed Seed Data if database is completely fresh
            seedInitialData(conn);

            System.out.println("Database tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Check if we already have teams
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM teams");
            if (rs.next() && rs.getInt(1) > 0) {
                rs.close();
                return; // already seeded
            }
            rs.close();

            System.out.println("Seeding demo data into SQLite database...");

            // Insert Judges
            String insertJudge = "INSERT INTO judges (name, domain, email, phone, password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertJudge)) {
                // Judge 1: AI/ML
                ps.setString(1, "Dr. Alice Vance");
                ps.setString(2, "AI/ML");
                ps.setString(3, "alice@hackathon.com");
                ps.setString(4, "+1234567890");
                ps.setString(5, "password");
                ps.executeUpdate();

                // Judge 2: Web Development
                ps.setString(1, "Bob Miller");
                ps.setString(2, "Web Development");
                ps.setString(3, "bob@hackathon.com");
                ps.setString(4, "+1234567891");
                ps.setString(5, "password");
                ps.executeUpdate();

                // Judge 3: Android
                ps.setString(1, "Charlie Smith");
                ps.setString(2, "Android");
                ps.setString(3, "charlie@hackathon.com");
                ps.setString(4, "+1234567892");
                ps.setString(5, "password");
                ps.executeUpdate();

                // Judge 4: IoT
                ps.setString(1, "Diana Prince");
                ps.setString(2, "IoT");
                ps.setString(3, "diana@hackathon.com");
                ps.setString(4, "+1234567893");
                ps.setString(5, "password");
                ps.executeUpdate();
            }

            // Insert Teams and Projects
            String[] teamNames = {"Byte Busters", "Dev Dynamos", "Droid Squad", "Sensors & Co", "Cyber Knights"};
            String[] leaders = {"Jatin Sharma", "Priya Patel", "Aman Verma", "Rohan Das", "Sneha Roy"};
            String[] colleges = {"IIT Bombay", "BITS Pilani", "Delhi Technological University", "VIT Vellore", "NIT Trichy"};
            String[] phones = {"9876543210", "9876543211", "9876543212", "9876543213", "9876543214"};
            String[] emails = {"jatin@iitb.ac.in", "priya@bits.ac.in", "aman@dtu.ac.in", "rohan@vit.ac.in", "sneha@nit.ac.in"};

            String[] projectTitles = {
                "EcoPredict AI - Climate Change Modeling",
                "MediSphere - Healthcare Management Portal",
                "FitTrac - Wearable Exercise Companion",
                "SmartFarm - Automated Irrigation System",
                "GuardShield - Zero Trust Access Control"
            };
            String[] domains = {"AI/ML", "Web Development", "Android", "IoT", "Cyber Security"};
            String[] desc = {
                "An AI tool to predict crop health patterns based on atmospheric humidity and satellite imagery.",
                "A full-stack medical application connecting doctors, patients, and pharmacies in real-time.",
                "An Android health app tracking workouts, calories, and syncs with smart bands.",
                "An IoT-based crop irrigation solution with real-time moisture reading and automatic valves.",
                "A cybersecurity solution implementing single sign-on with decentralized MFA certificates."
            };

            String insertTeam = "INSERT INTO teams (team_name, leader_name, college, phone, email) VALUES (?, ?, ?, ?, ?)";
            String insertParticipant = "INSERT INTO participants (team_id, name, branch, semester, email) VALUES (?, ?, ?, ?, ?, ?)";
            String insertProject = "INSERT INTO projects (team_id, title, domain, description, github_link, ppt_link) VALUES (?, ?, ?, ?, ?, ?, ?)";

            for (int i = 0; i < teamNames.length; i++) {
                // Insert Team
                int teamId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertTeam, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, teamNames[i]);
                    ps.setString(2, leaders[i]);
                    ps.setString(3, colleges[i]);
                    ps.setString(4, phones[i]);
                    ps.setString(5, emails[i]);
                    ps.executeUpdate();
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            teamId = generatedKeys.getInt(1);
                        }
                    }
                }

                // Insert Leader as Participant
                try (PreparedStatement ps = conn.prepareStatement(insertParticipant)) {
                    ps.setInt(1, teamId);
                    ps.setString(2, leaders[i]);
                    ps.setString(3, "Computer Science");
                    ps.setString(4, "6th");
                    ps.setString(5, emails[i]);
                    ps.executeUpdate();
                }

                // Add 2 more members per team
                try (PreparedStatement ps = conn.prepareStatement(insertParticipant)) {
                    ps.setInt(1, teamId);
                    ps.setString(2, "Member A of " + teamNames[i]);
                    ps.setString(3, "Information Technology");
                    ps.setString(4, "6th");
                    ps.setString(5, "membera." + teamNames[i].toLowerCase().replace(" ", "") + "@college.edu");
                    ps.executeUpdate();

                    ps.setInt(1, teamId);
                    ps.setString(2, "Member B of " + teamNames[i]);
                    ps.setString(3, "Electronics");
                    ps.setString(4, "6th");
                    ps.setString(5, "memberb." + teamNames[i].toLowerCase().replace(" ", "") + "@college.edu");
                    ps.executeUpdate();
                }

                // Insert Project
                int projectId = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertProject, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, teamId);
                    ps.setString(2, projectTitles[i]);
                    ps.setString(3, domains[i]);
                    ps.setString(4, desc[i]);
                    ps.setString(5, "https://github.com/hackathon/" + teamNames[i].toLowerCase().replace(" ", "-"));
                    ps.setString(6, "https://slidesshare.net/hackathon/" + teamNames[i].toLowerCase().replace(" ", "-"));
                    ps.executeUpdate();
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            projectId = generatedKeys.getInt(1);
                        }
                    }
                }

                // Seed some assignments and evaluations
                // Assign Judge 1 (AI/ML) to Project 1 (AI/ML) and Project 5 (Cyber)
                // Assign Judge 2 (Web Dev) to Project 2 (Web Dev) and Project 3 (Android)
                // Assign Judge 3 (Android) to Project 3 (Android) and Project 4 (IoT)
                // Assign Judge 4 (IoT) to Project 4 (IoT) and Project 1 (AI/ML)
            }

            // Populate assignments & evaluations manually
            stmt.execute("INSERT INTO assignments (project_id, judge_id) VALUES (1, 1)"); // Proj 1 to Judge 1
            stmt.execute("INSERT INTO assignments (project_id, judge_id) VALUES (5, 1)"); // Proj 5 to Judge 1
            stmt.execute("INSERT INTO assignments (project_id, judge_id) VALUES (2, 2)"); // Proj 2 to Judge 2
            stmt.execute("INSERT INTO assignments (project_id, judge_id) VALUES (3, 2)"); // Proj 3 to Judge 2
            stmt.execute("INSERT INTO assignments (project_id, judge_id) VALUES (3, 3)"); // Proj 3 to Judge 3
            stmt.execute("INSERT INTO assignments (project_id, judge_id) VALUES (4, 3)"); // Proj 4 to Judge 3
            stmt.execute("INSERT INTO assignments (project_id, judge_id) VALUES (4, 4)"); // Proj 4 to Judge 4
            stmt.execute("INSERT INTO assignments (project_id, judge_id) VALUES (1, 4)"); // Proj 1 to Judge 4

            // Add scores
            // Innovation, Technical, UIUX, Presentation, Impact, Total
            // Score range 0-10 per category
            stmt.execute("INSERT INTO evaluations (project_id, judge_id, innovation, technical, uiux, presentation, impact, total_score) " +
                    "VALUES (1, 1, 9, 8, 8, 9, 9, 43)");
            stmt.execute("INSERT INTO evaluations (project_id, judge_id, innovation, technical, uiux, presentation, impact, total_score) " +
                    "VALUES (1, 4, 8, 8, 7, 8, 9, 40)"); // Proj 1 Avg = 41.5
            
            stmt.execute("INSERT INTO evaluations (project_id, judge_id, innovation, technical, uiux, presentation, impact, total_score) " +
                    "VALUES (2, 2, 7, 7, 8, 8, 8, 38)"); // Proj 2 Avg = 38
            
            stmt.execute("INSERT INTO evaluations (project_id, judge_id, innovation, technical, uiux, presentation, impact, total_score) " +
                    "VALUES (3, 2, 8, 9, 9, 7, 8, 41)");
            stmt.execute("INSERT INTO evaluations (project_id, judge_id, innovation, technical, uiux, presentation, impact, total_score) " +
                    "VALUES (3, 3, 9, 8, 8, 8, 9, 42)"); // Proj 3 Avg = 41.5
            
            stmt.execute("INSERT INTO evaluations (project_id, judge_id, innovation, technical, uiux, presentation, impact, total_score) " +
                    "VALUES (4, 3, 8, 7, 6, 7, 9, 37)");
            stmt.execute("INSERT INTO evaluations (project_id, judge_id, innovation, technical, uiux, presentation, impact, total_score) " +
                    "VALUES (4, 4, 9, 8, 8, 8, 9, 42)"); // Proj 4 Avg = 39.5
            
            stmt.execute("INSERT INTO evaluations (project_id, judge_id, innovation, technical, uiux, presentation, impact, total_score) " +
                    "VALUES (5, 1, 9, 9, 8, 9, 9, 44)"); // Proj 5 Avg = 44
        }
    }
}
