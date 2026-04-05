package service;

import dao.MilestoneDAO;
import dao.PaymentDAOImpl;
import dao.ProjectDAO;
import model.Milestone;
import model.Project;

import java.util.List;

public class ProjectService {

    private final ProjectDAO    projectDAO   = new ProjectDAO();
    private final MilestoneDAO  milestoneDAO = new MilestoneDAO();
    private final PaymentDAOImpl paymentDAO  = new PaymentDAOImpl(); // ← fixed

    // Create project
    public boolean createProject(String name, String location,
                                  int contractorId, double budget) {
        return projectDAO.addProject(name, location, contractorId, budget);
    }

    // Add milestone AND auto-create a payment record for it
    public boolean addMilestoneWithPayment(int projectId,
                                            String description,
                                            double amount) {
        // Step 1: Add milestone
        boolean milestoneAdded = milestoneDAO.addMilestone(projectId, description, amount);
        if (!milestoneAdded) return false;

        // Step 2: Find the milestone we just created
        List<Milestone> milestones = milestoneDAO.getMilestonesByProject(projectId);
        if (milestones.isEmpty()) return false;

        // Get the last added milestone
        Milestone latest = milestones.get(milestones.size() - 1);

        // Step 3: Auto-create a HOLD payment for it
        return paymentDAO.createPayment(latest.getMilestoneId(), amount);
    }

    // Get all projects
    public List<Project> getAllProjects() {
        return projectDAO.getAllProjects();
    }

    // Get milestones for a project
    public List<Milestone> getMilestones(int projectId) {
        return milestoneDAO.getMilestonesByProject(projectId);
    }
}