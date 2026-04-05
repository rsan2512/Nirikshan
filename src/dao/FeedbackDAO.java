package dao;

import model.PublicFeedback;
import java.util.List;

public interface FeedbackDAO {
    void addFeedback(PublicFeedback feedback);
    List<PublicFeedback> getAllFeedback();
}