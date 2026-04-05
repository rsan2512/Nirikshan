package model;

public class Milestone {
    private int milestoneId;
    private int projectId;
    private String description;
    private double amount;
    private String status;

    public Milestone() {
    }

    public Milestone(int milestoneId, int projectId, String description, double amount, String status) {
        this.milestoneId = milestoneId;
        this.projectId = projectId;
        this.description = description;
        this.amount = amount;
        this.status = status;
    }

    public int getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(int milestoneId) {
        this.milestoneId = milestoneId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public double getBudget() {
        return amount;
    }

    public void setBudget(double budget) {
        this.amount = budget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Milestone{" +
                "milestoneId=" + milestoneId +
                ", projectId=" + projectId +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                '}';
    }
}
