package dao;

import model.Inspection;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InspectionDAOImpl implements InspectionDAO {

    // Inspector submits inspection result
    public boolean addInspection(int milestoneId, int inspectorId,
                                  String result, String remarks) {
        String sql = "INSERT INTO inspections "
                   + "(milestone_id, inspector_id, result, remarks) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, milestoneId);
            ps.setInt   (2, inspectorId);
            ps.setString(3, result);
            ps.setString(4, remarks);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Add inspection error: " + e.getMessage());
            return false;
        }
    }

    // Get all inspections submitted by a specific inspector
    public List<Inspection> getInspectionsByInspector(int inspectorId) {
        List<Inspection> list = new ArrayList<>();
        String sql = "SELECT * FROM inspections WHERE inspector_id = ? "
                   + "ORDER BY inspected_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inspectorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Inspection(
                    rs.getInt   ("inspection_id"),
                    rs.getInt   ("milestone_id"),
                    rs.getInt   ("inspector_id"),
                    rs.getString("result"),
                    rs.getString("remarks")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get by inspector error: " + e.getMessage());
        }
        return list;
    }

    // Get latest inspection result for a milestone
    public String getLatestResult(int milestoneId) {
        String sql = "SELECT result FROM inspections "
                   + "WHERE milestone_id = ? "
                   + "ORDER BY inspected_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, milestoneId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("result");

        } catch (SQLException e) {
            System.out.println("Get latest result error: " + e.getMessage());
        }
        return null;
    }

    // Get all inspections for a milestone
    @Override
    public List<Inspection> getInspectionsByMilestoneId(int milestoneId) {
        List<Inspection> list = new ArrayList<>();
        String sql = "SELECT * FROM inspections WHERE milestone_id = ? "
                   + "ORDER BY inspected_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, milestoneId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Inspection(
                    rs.getInt   ("inspection_id"),
                    rs.getInt   ("milestone_id"),
                    rs.getInt   ("inspector_id"),
                    rs.getString("result"),
                    rs.getString("remarks")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get by milestone error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void addInspection(Inspection inspection) {
        String sql = "INSERT INTO inspections "
                   + "(milestone_id, inspector_id, result, remarks) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, inspection.getMilestoneId());
            ps.setInt   (2, inspection.getInspectorId());
            ps.setString(3, inspection.getResult());
            ps.setString(4, inspection.getRemarks());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Add inspection error: " + e.getMessage());
        }
    }

    @Override
    public Inspection getInspectionById(int inspectionId) {
        String sql = "SELECT * FROM inspections WHERE inspection_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inspectionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Inspection(
                    rs.getInt   ("inspection_id"),
                    rs.getInt   ("milestone_id"),
                    rs.getInt   ("inspector_id"),
                    rs.getString("result"),
                    rs.getString("remarks")
                );
            }
        } catch (SQLException e) {
            System.out.println("Get inspection error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void updateInspection(Inspection inspection) {
        String sql = "UPDATE inspections SET result = ?, remarks = ? "
                   + "WHERE inspection_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, inspection.getResult());
            ps.setString(2, inspection.getRemarks());
            ps.setInt   (3, inspection.getInspectionId());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Update inspection error: " + e.getMessage());
        }
    }

    @Override
    public void deleteInspection(int inspectionId) {
        String sql = "DELETE FROM inspections WHERE inspection_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inspectionId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Delete inspection error: " + e.getMessage());
        }
    }
}