package com.hackathon.manager.model;

public class Project {
    private int projectId;
    private int teamId;
    private String title;
    private String domain;
    private String description;
    private String githubLink;
    private String pptLink;

    public Project() {}

    public Project(int projectId, int teamId, String title, String domain, String description, String githubLink, String pptLink) {
        this.projectId = projectId;
        this.teamId = teamId;
        this.title = title;
        this.domain = domain;
        this.description = description;
        this.githubLink = githubLink;
        this.pptLink = pptLink;
    }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGithubLink() { return githubLink; }
    public void setGithubLink(String githubLink) { this.githubLink = githubLink; }

    public String getPptLink() { return pptLink; }
    public void setPptLink(String pptLink) { this.pptLink = pptLink; }

    @Override
    public String toString() {
        return title;
    }
}
