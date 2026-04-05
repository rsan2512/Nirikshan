package dao;

import model.Inspection;
import java.util.List;

public interface InspectionDAO {
    void addInspection(Inspection inspection);
    Inspection getInspectionById(int inspectionId);
    List<Inspection> getInspectionsByMilestoneId(int milestoneId);
    void updateInspection(Inspection inspection);
    void deleteInspection(int inspectionId);
}