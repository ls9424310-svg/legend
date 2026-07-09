package com.hackathon.manager.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.hackathon.manager.db.DatabaseHelper;
import com.hackathon.manager.model.Project;

public class Recommender {

    public static class Recommendation {
        private final Project project;
        private final boolean isRecommended; // true if domain matches judge's expertise

        public Recommendation(Project project, boolean isRecommended) {
            this.project = project;
            this.isRecommended = isRecommended;
        }

        public Project getProject() { return project; }
        public boolean isRecommended() { return isRecommended; }

        @Override
        public String toString() {
            return project.getTitle() + (isRecommended ? " ★ [Expertise Match]" : "");
        }
    }

    /**
     * Recommends projects for a given judge.
     * Matches project domain with judge domain first, followed by other projects.
     */
    public static List<Recommendation> getRecommendations(int judgeId) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            // 1. Get judge's domain
            String judgeDomain = "";
            String queryJudge = "SELECT domain FROM judges WHERE judge_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(queryJudge)) {
                ps.setInt(1, judgeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        judgeDomain = rs.getString("domain");
                    }
                }
            }

            // 2. Query all projects
            String queryProj = "SELECT p.*, t.team_name FROM projects p JOIN teams t ON t.team_id = p.team_id";
            try (PreparedStatement ps = conn.prepareStatement(queryProj);
                 ResultSet rs = ps.executeQuery()) {
                
                List<Recommendation> matching = new ArrayList<>();
                List<Recommendation> others = new ArrayList<>();

                while (rs.next()) {
                    Project proj = new Project(
                        rs.getInt("project_id"),
                        rs.getInt("team_id"),
                        rs.getString("title"),
                        rs.getString("domain"),
                        rs.getString("description"),
                        rs.getString("github_link"),
                        rs.getString("ppt_link")
                    );

                    boolean isMatch = proj.getDomain().equalsIgnoreCase(judgeDomain);
                    Recommendation rec = new Recommendation(proj, isMatch);
                    
                    if (isMatch) {
                        matching.add(rec);
                    } else {
                        others.add(rec);
                    }
                }

                // Add matching first, then others
                recommendations.addAll(matching);
                recommendations.addAll(others);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch recommendations: " + e.getMessage());
            e.printStackTrace();
        }

        return recommendations;
    }
}
