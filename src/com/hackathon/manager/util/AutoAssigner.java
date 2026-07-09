package com.hackathon.manager.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hackathon.manager.db.DatabaseHelper;

public class AutoAssigner {

    /**
     * Automatically assigns judges to projects.
     * Wipes current assignments and distributes judges randomly.
     * Each project gets 'judgesPerProject' judges.
     */
    public static boolean autoAssign(int judgesPerProject) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Wipe existing assignments & evaluations (due to cascade or manual delete)
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM assignments");
                stmt.executeUpdate("DELETE FROM evaluations");
            }

            // 2. Fetch all project IDs
            List<Integer> projectIds = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT project_id FROM projects")) {
                while (rs.next()) {
                    projectIds.add(rs.getInt("project_id"));
                }
            }

            // 3. Fetch all judge IDs
            List<Integer> judgeIds = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT judge_id FROM judges")) {
                while (rs.next()) {
                    judgeIds.add(rs.getInt("judge_id"));
                }
            }

            if (judgeIds.isEmpty() || projectIds.isEmpty()) {
                conn.rollback();
                return false;
            }

            // Limit judgesPerProject if it exceeds total judges
            int actualJudgesPerProj = Math.min(judgesPerProject, judgeIds.size());

            // 4. Perform assignments
            String insertAssign = "INSERT INTO assignments (project_id, judge_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertAssign)) {
                for (int projId : projectIds) {
                    // Shuffle judges for this project to get random selection
                    Collections.shuffle(judgeIds);
                    for (int i = 0; i < actualJudgesPerProj; i++) {
                        ps.setInt(1, projId);
                        ps.setInt(2, judgeIds.get(i));
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }

            conn.commit();
            System.out.println("Auto-assignment completed successfully.");
            return true;
        } catch (Exception e) {
            System.err.println("Auto-assignment failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
