package model;

public class Inspection {
    private int    inspectionId;
    private int    milestoneId;
    private int    inspectorId;
    private String result;
    private String remarks;

    // Constructor
    public Inspection(int inspectionId, int milestoneId,
                      int inspectorId, String result, String remarks) {
        this.inspectionId = inspectionId;
        this.milestoneId  = milestoneId;
        this.inspectorId  = inspectorId;
        this.result       = result;
        this.remarks      = remarks;
    }

    // Getters
    public int    getInspectionId() { return inspectionId; }
    public int    getMilestoneId()  { return milestoneId; }
    public int    getInspectorId()  { return inspectorId; }
    public String getResult()       { return result; }
    public String getRemarks()      { return remarks; }

    @Override
    public String toString() {
        return "Inspection #" + inspectionId + " → " + result;
    }
}