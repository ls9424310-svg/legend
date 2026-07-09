package com.hackathon.manager.model;

public class Evaluation {
    private int evaluationId;
    private int projectId;
    private int judgeId;
    private int innovation;
    private int technical;
    private int uiux;
    private int presentation;
    private int impact;
    private int totalScore;

    public Evaluation() {}

    public Evaluation(int evaluationId, int projectId, int judgeId, int innovation, int technical, int uiux, int presentation, int impact, int totalScore) {
        this.evaluationId = evaluationId;
        this.projectId = projectId;
        this.judgeId = judgeId;
        this.innovation = innovation;
        this.technical = technical;
        this.uiux = uiux;
        this.presentation = presentation;
        this.impact = impact;
        this.totalScore = totalScore;
    }

    public int getEvaluationId() { return evaluationId; }
    public void setEvaluationId(int evaluationId) { this.evaluationId = evaluationId; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getJudgeId() { return judgeId; }
    public void setJudgeId(int judgeId) { this.judgeId = judgeId; }

    public int getInnovation() { return innovation; }
    public void setInnovation(int innovation) { this.innovation = innovation; }

    public int getTechnical() { return technical; }
    public void setTechnical(int technical) { this.technical = technical; }

    public int getUiux() { return uiux; }
    public void setUiux(int uiux) { this.uiux = uiux; }

    public int getPresentation() { return presentation; }
    public void setPresentation(int presentation) { this.presentation = presentation; }

    public int getImpact() { return impact; }
    public void setImpact(int impact) { this.impact = impact; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
}
