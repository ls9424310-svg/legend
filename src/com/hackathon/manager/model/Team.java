package com.hackathon.manager.model;

public class Team {
    private int teamId;
    private String teamName;
    private String leaderName;
    private String college;
    private String phone;
    private String email;

    public Team() {}

    public Team(int teamId, String teamName, String leaderName, String college, String phone, String email) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.leaderName = leaderName;
        this.college = college;
        this.phone = phone;
        this.email = email;
    }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return teamName;
    }
}
