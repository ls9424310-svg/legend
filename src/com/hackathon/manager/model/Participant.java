package com.hackathon.manager.model;

public class Participant {
    private int participantId;
    private int teamId;
    private String name;
    private String branch;
    private String semester;
    private String email;

    public Participant() {}

    public Participant(int participantId, int teamId, String name, String branch, String semester, String email) {
        this.participantId = participantId;
        this.teamId = teamId;
        this.name = name;
        this.branch = branch;
        this.semester = semester;
        this.email = email;
    }

    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return name;
    }
}
