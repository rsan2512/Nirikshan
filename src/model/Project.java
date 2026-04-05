package model;

public class Project {
    private int    projectId;
    private String name;
    private String location;
    private int    contractorId;
    private double totalBudget;
    private String status;

    // Constructor
    public Project(int projectId, String name, String location,
                   int contractorId, double totalBudget, String status) {
        this.projectId    = projectId;
        this.name         = name;
        this.location     = location;
        this.contractorId = contractorId;
        this.totalBudget  = totalBudget;
        this.status       = status;
    }

    // Getters
    public int    getProjectId()    { return projectId; }
    public String getName()         { return name; }
    public String getLocation()     { return location; }
    public int    getContractorId() { return contractorId; }
    public double getTotalBudget()  { return totalBudget; }
    public String getStatus()       { return status; }

    @Override
    public String toString() {
        return name + " — " + location;
    }
}