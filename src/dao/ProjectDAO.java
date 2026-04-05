package dao;

import model.Project;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    // Add a new project
    public boolean addProject(String name, String location,
                               int contractorId, double budget) {
        String sql = "INSERT INTO projects (name, location, contractor_id, total_budget) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, location);
            ps.setInt   (3, contractorId);
            ps.setDouble(4, budget);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Add project error: " + e.getMessage());
            return false;
        }
    }

    // Get all projects
    public List<Project> getAllProjects() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM projects ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Project(
                    rs.getInt   ("project_id"),
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getInt   ("contractor_id"),
                    rs.getDouble("total_budget"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get projects error: " + e.getMessage());
        }
        return list;
    }

    // Get projects by contractor
    public List<Project> getProjectsByContractor(int contractorId) {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE contractor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contractorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Project(
                    rs.getInt   ("project_id"),
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getInt   ("contractor_id"),
                    rs.getDouble("total_budget"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get contractor projects error: " + e.getMessage());
        }
        return list;
    }

    // Update project status
    public boolean updateStatus(int projectId, String status) {
        String sql = "UPDATE projects SET status = ? WHERE project_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt   (2, projectId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Update status error: " + e.getMessage());
            return false;
        }
    }
}