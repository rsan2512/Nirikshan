package dao;

import model.Payment;
import java.util.List;

public interface PaymentDAO {
    boolean createPayment(int milestoneId, double amount);
    void addPayment(Payment payment);
    Payment getPaymentById(int paymentId);
    List<Payment> getPaymentsByMilestoneId(int milestoneId);
    void updatePayment(Payment payment);
    void deletePayment(int paymentId);
}