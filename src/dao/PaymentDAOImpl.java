package dao;

import model.Payment;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {

    // Create a HOLD payment when milestone is added
    @Override
    public boolean createPayment(int milestoneId, double amount) {
        String sql = "INSERT INTO payments (milestone_id, amount, status) "
                   + "VALUES (?, ?, 'HOLD')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, milestoneId);
            ps.setDouble(2, amount);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Create payment error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void addPayment(Payment payment) {
        String sql = "INSERT INTO payments (milestone_id, amount, status) "
                   + "VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, payment.getMilestoneId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getStatus());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Add payment error: " + e.getMessage());
        }
    }

    @Override
    public Payment getPaymentById(int paymentId) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, paymentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Payment(
                    rs.getInt   ("payment_id"),
                    rs.getInt   ("milestone_id"),
                    rs.getDouble("amount"),
                    rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.out.println("Get payment error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Payment> getPaymentsByMilestoneId(int milestoneId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE milestone_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, milestoneId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Payment(
                    rs.getInt   ("payment_id"),
                    rs.getInt   ("milestone_id"),
                    rs.getDouble("amount"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get payments error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void updatePayment(Payment payment) {
        String sql = "UPDATE payments SET status = ?, released_at = ? "
                   + "WHERE payment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, payment.getStatus());
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt   (3, payment.getPaymentId());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Update payment error: " + e.getMessage());
        }
    }

    @Override
    public void deletePayment(int paymentId) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, paymentId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Delete payment error: " + e.getMessage());
        }
    }
}