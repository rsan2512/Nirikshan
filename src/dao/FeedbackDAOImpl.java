package dao;

import model.PublicFeedback;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAOImpl implements FeedbackDAO {

    // Submit public feedback
    public boolean addFeedback(int projectId, int rating, String comment) {
        String sql = "INSERT INTO public_feedback (project_id, rating, comment) "
                   + "VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, projectId);
            ps.setInt   (2, rating);
            ps.setString(3, comment);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Add feedback error: " + e.getMessage());
            return false;
        }
    }

    // Get average rating for a project
    public double getAverageRating(int projectId) {
        String sql = "SELECT COALESCE(AVG(rating), 0) as avg_rating "
                   + "FROM public_feedback WHERE project_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("avg_rating");

        } catch (SQLException e) {
            System.out.println("Get avg rating error: " + e.getMessage());
        }
        return 0.0;
    }

    // Get all feedback for a project
    public List<PublicFeedback> getFeedbackByProject(int projectId) {
        List<PublicFeedback> list = new ArrayList<>();
        String sql = "SELECT * FROM public_feedback WHERE project_id = ? "
                   + "ORDER BY submitted_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PublicFeedback(
                    rs.getInt   ("feedback_id"),
                    rs.getInt   ("project_id"),
                    rs.getInt   ("rating"),
                    rs.getString("comment")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get feedback error: " + e.getMessage());
        }
        return list;
    }

    // Interface methods
    @Override
    public void addFeedback(PublicFeedback feedback) {
        addFeedback(feedback.getProjectId(),
                    feedback.getRating(),
                    feedback.getComment());
    }

    @Override
    public List<PublicFeedback> getAllFeedback() {
        List<PublicFeedback> list = new ArrayList<>();
        String sql = "SELECT * FROM public_feedback ORDER BY submitted_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new PublicFeedback(
                    rs.getInt   ("feedback_id"),
                    rs.getInt   ("project_id"),
                    rs.getInt   ("rating"),
                    rs.getString("comment")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get all feedback error: " + e.getMessage());
        }
        return list;
    }
}