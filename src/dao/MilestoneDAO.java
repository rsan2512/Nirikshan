package dao;

import model.Milestone;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MilestoneDAO {

    // Add milestone to a project
    public boolean addMilestone(int projectId, String description, double amount) {
        String sql = "INSERT INTO milestones (project_id, description, amount) "
                   + "VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, projectId);
            ps.setString(2, description);
            ps.setDouble(3, amount);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Add milestone error: " + e.getMessage());
            return false;
        }
    }

    // Get all milestones for a project
    public List<Milestone> getMilestonesByProject(int projectId) {
        List<Milestone> list = new ArrayList<>();
        String sql = "SELECT * FROM milestones WHERE project_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Milestone(
                    rs.getInt   ("milestone_id"),
                    rs.getInt   ("project_id"),
                    rs.getString("description"),
                    rs.getDouble("amount"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get milestones error: " + e.getMessage());
        }
        return list;
    }

    // Get milestones by status — used by Inspector Dashboard
    public List<Milestone> getMilestonesByStatus(String status) {
        List<Milestone> list = new ArrayList<>();
        String sql = "SELECT * FROM milestones WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Milestone(
                    rs.getInt   ("milestone_id"),
                    rs.getInt   ("project_id"),
                    rs.getString("description"),
                    rs.getDouble("amount"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get by status error: " + e.getMessage());
        }
        return list;
    }

    // Contractor submits a milestone for inspection
    public boolean submitMilestone(int milestoneId) {
        String sql = "UPDATE milestones SET status = 'SUBMITTED' "
                   + "WHERE milestone_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, milestoneId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Submit milestone error: " + e.getMessage());
            return false;
        }
    }

    // Update milestone status
    public boolean updateStatus(int milestoneId, String status) {
        String sql = "UPDATE milestones SET status = ? WHERE milestone_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt   (2, milestoneId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Update milestone error: " + e.getMessage());
            return false;
        }
    }
}