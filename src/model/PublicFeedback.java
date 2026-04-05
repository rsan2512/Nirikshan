package model;

public class PublicFeedback {
    private int    feedbackId;
    private int    projectId;
    private int    rating;
    private String comment;

    // Constructor
    public PublicFeedback(int feedbackId, int projectId,
                          int rating, String comment) {
        this.feedbackId = feedbackId;
        this.projectId  = projectId;
        this.rating     = rating;
        this.comment    = comment;
    }

    // Getters
    public int    getFeedbackId() { return feedbackId; }
    public int    getProjectId()  { return projectId; }
    public int    getRating()     { return rating; }
    public String getComment()    { return comment; }

    @Override
    public String toString() {
        return "Feedback #" + feedbackId + " ⭐" + rating;
    }
}