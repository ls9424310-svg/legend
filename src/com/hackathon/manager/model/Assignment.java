package com.hackathon.manager.model;

public class Assignment {
    private int id;
    private int projectId;
    private int judgeId;

    public Assignment() {}

    public Assignment(int id, int projectId, int judgeId) {
        this.id = id;
        this.projectId = projectId;
        this.judgeId = judgeId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getJudgeId() { return judgeId; }
    public void setJudgeId(int judgeId) { this.judgeId = judgeId; }
}
