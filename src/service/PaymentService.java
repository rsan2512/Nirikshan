package service;

import util.DBConnection;
import java.sql.*;

public class PaymentService {

    /**
     * PAYMENT RELEASE RULES:
     * Rule 1 → Latest inspection must be APPROVED
     * Rule 2 → Average public rating must be >= 3
     * If both pass → payment RELEASED + milestone marked PAID
     * If any fail  → payment stays HOLD
     * Uses transaction → either ALL updates succeed or NONE do
     */
    public String releasePayment(int milestoneId) {
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // ── RULE 1: Check latest inspection result ──
            String inspSql =
                "SELECT result FROM inspections " +
                "WHERE milestone_id = ? " +
                "ORDER BY inspected_at DESC LIMIT 1";

            PreparedStatement inspPs = conn.prepareStatement(inspSql);
            inspPs.setInt(1, milestoneId);
            ResultSet inspRs = inspPs.executeQuery();

            if (!inspRs.next()) {
                conn.rollback();
                return "HOLD|No inspection found for this milestone.";
            }

            String inspResult = inspRs.getString("result");
            if (!inspResult.equals("APPROVED")) {
                conn.rollback();
                return "HOLD|Inspection status is " + inspResult + ". Must be APPROVED.";
            }

            // ── RULE 2: Check average public rating ──
            String ratingSql =
                "SELECT COALESCE(AVG(f.rating), 0) as avg_rating " +
                "FROM public_feedback f " +
                "JOIN milestones m ON f.project_id = m.project_id " +
                "WHERE m.milestone_id = ?";

            PreparedStatement ratingPs = conn.prepareStatement(ratingSql);
            ratingPs.setInt(1, milestoneId);
            ResultSet ratingRs = ratingPs.executeQuery();

            double avgRating = 0;
            if (ratingRs.next()) {
                avgRating = ratingRs.getDouble("avg_rating");
            }

            if (avgRating < 3.0) {
                conn.rollback();
                return "HOLD|Public rating is " + String.format("%.1f", avgRating)
                     + "/5. Minimum 3.0 required.";
            }

            // ── BOTH RULES PASSED → Release Payment ──

            // Step 1: Update payment status to RELEASED
            String updatePaymentSql =
                "UPDATE payments SET status = 'RELEASED', released_at = NOW() " +
                "WHERE milestone_id = ?";
            PreparedStatement payPs = conn.prepareStatement(updatePaymentSql);
            payPs.setInt(1, milestoneId);
            payPs.executeUpdate();

            // Step 2: Update milestone status to PAID
            String updateMilestoneSql =
                "UPDATE milestones SET status = 'PAID' WHERE milestone_id = ?";
            PreparedStatement milPs = conn.prepareStatement(updateMilestoneSql);
            milPs.setInt(1, milestoneId);
            milPs.executeUpdate();

            conn.commit(); // COMMIT TRANSACTION ✅
            return "RELEASED|Payment successfully released for milestone #"
                 + milestoneId
                 + " (Rating: " + String.format("%.1f", avgRating) + "/5)";

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // ROLLBACK on error
            } catch (SQLException ex) {
                System.out.println("Rollback error: " + ex.getMessage());
            }
            return "ERROR|" + e.getMessage();

        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Close error: " + e.getMessage());
            }
        }
    }

    /**
     * Checks if a milestone is eligible for payment
     * Returns a readable status message shown in the UI
     */
    public String checkEligibility(int milestoneId) {
        try (Connection conn = DBConnection.getConnection()) {

            // Check inspection
            String inspSql =
                "SELECT result FROM inspections " +
                "WHERE milestone_id = ? " +
                "ORDER BY inspected_at DESC LIMIT 1";
            PreparedStatement inspPs = conn.prepareStatement(inspSql);
            inspPs.setInt(1, milestoneId);
            ResultSet inspRs = inspPs.executeQuery();

            String inspResult = "NOT INSPECTED";
            if (inspRs.next()) inspResult = inspRs.getString("result");

            // Check rating
            String ratingSql =
                "SELECT COALESCE(AVG(f.rating), 0) as avg_rating " +
                "FROM public_feedback f " +
                "JOIN milestones m ON f.project_id = m.project_id " +
                "WHERE m.milestone_id = ?";
            PreparedStatement ratingPs = conn.prepareStatement(ratingSql);
            ratingPs.setInt(1, milestoneId);
            ResultSet ratingRs = ratingPs.executeQuery();

            double avgRating = 0;
            if (ratingRs.next()) avgRating = ratingRs.getDouble("avg_rating");

            // Build eligibility report
            String inspIcon   = inspResult.equals("APPROVED") ? "✅" : "❌";
            String ratingIcon = avgRating >= 3.0             ? "✅" : "❌";

            return  inspIcon   + " Inspection : " + inspResult + "\n" +
                    ratingIcon + " Public Rating: " + String.format("%.1f", avgRating) + "/5" +
                    (avgRating == 0 ? " (No feedback yet)" : "");

        } catch (SQLException e) {
            return "❌ Error checking eligibility: " + e.getMessage();
        }
    }
}