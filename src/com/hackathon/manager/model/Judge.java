package com.hackathon.manager.model;

public class Judge {
    private int judgeId;
    private String name;
    private String domain;
    private String email;
    private String phone;

    public Judge() {}

    public Judge(int judgeId, String name, String domain, String email, String phone) {
        this.judgeId = judgeId;
        this.name = name;
        this.domain = domain;
        this.email = email;
        this.phone = phone;
    }

    public int getJudgeId() { return judgeId; }
    public void setJudgeId(int judgeId) { this.judgeId = judgeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return name + " (" + domain + ")";
    }
}
