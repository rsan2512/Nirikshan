# Business Services Layer Documentation

## Overview

The Service layer contains the core business logic of the application. Services coordinate between multiple DAOs, enforce business rules, manage transactions, and provide high-level operations.

**Location**: `src/service/`

## Service Classes

### 1. PaymentService

**Core Responsibility**: Manage payment authorization with complex business rules

#### Payment Release Logic

The `releasePayment()` method implements the critical business logic that determines when a contractor gets paid.

```java
public String releasePayment(int milestoneId)
```

**Purpose**: 
- Validate milestone inspection status
- Check public feedback rating
- Release payment if all conditions met
- Update payment and milestone status atomically

**Returns**: 
A status string in format: `"STATUS|MESSAGE"`
- `"RELEASED|Payment successfully released for milestone #1 (Rating: 4.5/5)"`
- `"HOLD|Inspection status is REJECTED. Must be APPROVED."`
- `"HOLD|Public rating is 2.3/5. Minimum 3.0 required."`
- `"ERROR|Connection timeout..."`

---

#### Business Rules for Payment Release

**Rule 1: Latest Inspection Must Be APPROVED**
```
Query: Get the most recent inspection for the milestone
Check: inspection.result == "APPROVED"
If: Any other result (REJECTED, NEEDS_REWORK) or no inspection
Then: Payment stays HOLD
```

**Rule 2: Average Public Rating ≥ 3.0/5**
```
Query: Calculate average of all public_feedback.rating for the project
Check: avg_rating >= 3.0
If: avg_rating < 3.0
Then: Payment stays HOLD
```

**Payment Release Decision Tree**:
```
                    START
                     │
          ┌──────────┴──────────┐
          │                     │
    Check Inspection      Check Rating
    (Latest APPROVED?)    (Avg >= 3.0?)
          │                     │
        YES                    YES
          └──────────┬──────────┘
                     │
              Both conditions met?
                     │
          ┌──────────┴──────────┐
         YES                    NO
          │                      │
      RELEASE              Keep HOLD
      Payment               Return reason
```

---

#### PaymentService Implementation Details

**Database Operations**:

1. **Check Latest Inspection** (SQL)
```sql
SELECT result FROM inspections 
WHERE milestone_id = ? 
ORDER BY inspected_at DESC LIMIT 1
```

2. **Check Average Rating** (SQL)
```sql
SELECT COALESCE(AVG(f.rating), 0) as avg_rating 
FROM public_feedback f 
JOIN milestones m ON f.project_id = m.project_id 
WHERE m.milestone_id = ?
```

3. **Update Payment Status** (if approved) (SQL)
```sql
UPDATE payments 
SET status = 'RELEASED', released_at = NOW() 
WHERE milestone_id = ?
```

4. **Update Milestone Status** (if approved) (SQL)
```sql
UPDATE milestones 
SET status = 'PAID' 
WHERE milestone_id = ?
```

**Transaction Safety**:
```java
conn.setAutoCommit(false);  // START TRANSACTION

try {
    // Perform multiple updates
    payment_update();
    milestone_update();
    conn.commit();          // ALL succeed together
} catch (SQLException e) {
    conn.rollback();        // ALL fail together
}
```

**Why Transactions Matter**:
- Prevents inconsistent state (e.g., payment released but milestone not marked PAID)
- Ensures atomicity: either all updates succeed or none do
- Protects against partial failures

---

#### PaymentService Usage Example

```java
// In AdminDashboard or PaymentScreen
PaymentService service = new PaymentService();
String result = service.releasePayment(5);  // Milestone ID 5

String[] parts = result.split("\\|");
String status = parts[0];    // "RELEASED", "HOLD", or "ERROR"
String message = parts[1];   // Detailed message

if ("RELEASED".equals(status)) {
    JOptionPane.showMessageDialog(this, 
        "Success: " + message, 
        "Payment Released", 
        JOptionPane.INFORMATION_MESSAGE);
    refreshPaymentList();
} else if ("HOLD".equals(status)) {
    JOptionPane.showMessageDialog(this, 
        "Cannot release: " + message, 
        "Payment on Hold", 
        JOptionPane.WARNING_MESSAGE);
} else {
    JOptionPane.showMessageDialog(this, 
        "Error: " + message, 
        "Error", 
        JOptionPane.ERROR_MESSAGE);
}
```

---

#### Payment Release Scenarios

**Scenario 1: Successful Payment Release**
```
Milestone #1: Foundation
├─ Latest Inspection: APPROVED ✅
├─ Project Average Rating: 4.2/5 ✅
└─ Result: PAYMENT RELEASED ✅

Response: "RELEASED|Payment successfully released for milestone #1 (Rating: 4.2/5)"
```

**Scenario 2: Inspection Rejected**
```
Milestone #2: Walls
├─ Latest Inspection: REJECTED ❌
├─ Project Average Rating: 4.8/5 ✅
└─ Result: PAYMENT HELD ❌

Response: "HOLD|Inspection status is REJECTED. Must be APPROVED."
```

**Scenario 3: Low Public Rating**
```
Milestone #3: Roof
├─ Latest Inspection: APPROVED ✅
├─ Project Average Rating: 2.1/5 ❌
└─ Result: PAYMENT HELD ❌

Response: "HOLD|Public rating is 2.1/5. Minimum 3.0 required."
```

**Scenario 4: No Inspection Yet**
```
Milestone #4: Interior
├─ Latest Inspection: NONE ❌
├─ Project Average Rating: 4.5/5 ✅
└─ Result: PAYMENT HELD ❌

Response: "HOLD|No inspection found for this milestone."
```

---

### 2. ProjectService

**Core Responsibility**: Manage project operations

```java
public class ProjectService {
    // Expected methods (based on architecture)
    public boolean createProject(String name, String location, int contractorId, double budget)
    public List<Project> getProjectsByContractor(int contractorId)
    public boolean updateProjectStatus(int projectId, String status)
    public List<Project> getAllProjects()
}
```

**Purpose**:
- Encapsulate project business logic
- Validate project data
- Coordinate project-related DAOs
- Provide unified project operations

**Potential Business Rules**:
- Validate budget is positive
- Prevent duplicate project names
- Validate contractor exists
- Update status based on milestone completion

---

## Service Layer Architecture

### Layering Strategy

```
┌─────────────────────────────────┐
│  UI Layer                       │
│  (Calls Service methods)        │
└─────────────────┬───────────────┘
                  │ Uses
┌─────────────────▼───────────────┐
│  Service Layer                  │
│  (PaymentService, etc.)         │
│  - Implements business rules    │
│  - Manages transactions         │
│  - Coordinates DAOs             │
└─────────────────┬───────────────┘
                  │ Uses
┌─────────────────▼───────────────┐
│  DAO Layer                      │
│  (UserDAO, ProjectDAO, etc.)    │
│  - Executes SQL                 │
│  - Maps to models               │
└─────────────────┬───────────────┘
                  │ Uses
┌─────────────────▼───────────────┐
│  Database (PostgreSQL)          │
└─────────────────────────────────┘
```

---

## Business Rules Implemented

### Payment Authorization Rules

| Rule | Description | Enforced By |
|------|-------------|------------|
| Inspection Requirement | Latest inspection must exist and be APPROVED | PaymentService |
| Public Feedback Minimum | Average rating must be ≥ 3.0/5 | PaymentService |
| Payment Atomicity | Payment and milestone status updated together | Database Transaction |
| Status Progression | Milestone moves APPROVED → PAID | PaymentService |

### Potential Business Rules

| Area | Rule | Status |
|------|------|--------|
| Project | Budget must be positive | ❌ Not enforced |
| Project | Contractor must exist | ❌ Not enforced |
| Milestone | Amount cannot exceed project budget | ❌ Not enforced |
| Milestone | Cannot add milestones to completed projects | ❌ Not enforced |
| Inspection | Only assigned inspector can inspect | ❌ Not enforced |
| Payment | Cannot release same payment twice | ⚠️ Partially (status check) |

---

## Transaction Management

### Why Transactions Matter

**Scenario Without Transactions**:
```
1. Update payment status = 'RELEASED'      ✅ Success
2. Update milestone status = 'PAID'        ❌ Connection lost!

Result: Inconsistent state - payment released but milestone not paid
```

**Scenario With Transactions**:
```
1. START TRANSACTION
2. Update payment status = 'RELEASED'      ✅ Success
3. Update milestone status = 'PAID'        ❌ Connection lost!
4. ROLLBACK (both changes undone)

Result: Consistent state - both or neither updated
```

### PaymentService Transaction Pattern

```java
Connection conn = null;
try {
    conn = DBConnection.getConnection();
    conn.setAutoCommit(false);  // START TRANSACTION
    
    // Step 1: Check conditions
    if (!inspectionApproved) {
        conn.rollback();
        return "HOLD|...";
    }
    
    if (avgRating < 3.0) {
        conn.rollback();
        return "HOLD|...";
    }
    
    // Step 2: Update payment
    updatePaymentSQL(...);
    
    // Step 3: Update milestone
    updateMilestoneSQL(...);
    
    conn.commit();  // COMMIT TRANSACTION
    return "RELEASED|...";
    
} catch (SQLException e) {
    conn.rollback();
    return "ERROR|...";
}
```

---

## Error Handling Strategy

### Current Approach

1. **Catch SQL Exceptions**
2. **Log to Console** (improvement needed)
3. **Return Meaningful Message**
4. **UI Displays Message**

### Example: PaymentService

```java
catch (SQLException e) {
    try {
        if (conn != null) conn.rollback();
    } catch (SQLException ex) {
        System.out.println("Rollback error: " + ex.getMessage());
    }
    return "ERROR|" + e.getMessage();
}
```

### Future Improvements

1. **Custom Exception Classes**
   ```java
   public class PaymentException extends Exception {
       public PaymentException(String message) {
           super(message);
       }
   }
   ```

2. **Proper Logging Framework** (Log4j, SLF4J)
   ```java
   logger.error("Payment release failed for milestone: " + milestoneId, e);
   ```

3. **Structured Error Codes**
   ```java
   "ERR_001|Inspection not found"
   "ERR_002|Rating below threshold"
   "ERR_999|Database connection failed"
   ```

---

## Service Usage Patterns

### Pattern 1: Direct Service Call from UI
```java
// In AdminDashboard
PaymentService paymentService = new PaymentService();
String result = paymentService.releasePayment(selectedMilestoneId);
if (result.startsWith("RELEASED")) {
    // Show success
}
```

### Pattern 2: Service with Validation
```java
// In ProjectService
public boolean createProject(String name, String location, 
                            int contractorId, double budget) {
    // Validate inputs
    if (name == null || name.isEmpty()) return false;
    if (budget <= 0) return false;
    
    // Call DAO
    ProjectDAO dao = new ProjectDAO();
    return dao.addProject(name, location, contractorId, budget);
}
```

### Pattern 3: Service with Transaction Coordination
```java
// Complex operation coordinating multiple DAOs
public boolean completeProjectMilestones(int projectId) {
    ProjectDAO projDAO = new ProjectDAO();
    MilestoneDAO milestoneDAO = new MilestoneDAO();
    PaymentDAO paymentDAO = new PaymentDAO();
    
    // Transaction would span multiple DAO calls
    // Currently handled at DAO level in PaymentService
}
```

---

## Best Practices

### ✅ DO

1. **Keep Business Logic in Services**
   ```java
   // In PaymentService
   public String releasePayment(int milestoneId) {
       // Business logic here
   }
   ```

2. **Use Transactions for Multi-Step Operations**
   ```java
   conn.setAutoCommit(false);
   // ... operations ...
   conn.commit();
   ```

3. **Provide Meaningful Return Values**
   ```java
   return "RELEASED|Payment for milestone #5 released successfully";
   ```

4. **Handle Exceptions Gracefully**
   ```java
   try {
       // ...
   } catch (SQLException e) {
       return "ERROR|" + e.getMessage();
   }
   ```

### ❌ DON'T

1. **Don't Put SQL in UI**
   ```java
   // ❌ Wrong - in UI class
   String sql = "SELECT * FROM users WHERE id = " + userId;
   ```

2. **Don't Mix Concerns**
   ```java
   // ❌ Wrong - Service doing UI stuff
   service.releasePayment(id);
   JOptionPane.showMessageDialog(...);
   ```

3. **Don't Skip Transaction Management**
   ```java
   // ❌ Wrong - partial updates
   updatePayment();
   updateMilestone();  // Fails after updatePayment succeeds
   ```

4. **Don't Ignore Exceptions**
   ```java
   // ❌ Wrong
   try {
       releasePayment(id);
   } catch (Exception e) {}
   ```

---

## Future Service Enhancements

1. **ProjectService Implementation**
   - Full CRUD operations for projects
   - Milestone management
   - Status validation

2. **MilestoneService**
   - Milestone submission workflow
   - Milestone completion logic
   - Budget tracking

3. **InspectionService**
   - Inspection assignment
   - Inspection workflow
   - Rework request handling

4. **ReportingService**
   - Payment reports
   - Project progress reports
   - Contractor performance analytics

5. **NotificationService**
   - Payment release notifications
   - Inspection reminders
   - Project milestone alerts

---

## Service Testing Strategy

### Unit Test Example
```java
@Test
public void testReleasePaymentApprovedInspection() {
    PaymentService service = new PaymentService();
    String result = service.releasePayment(1);
    
    assertTrue(result.startsWith("RELEASED"));
    assertTrue(result.contains("4.2"));  // Rating in response
}

@Test
public void testReleasePaymentRejectedInspection() {
    PaymentService service = new PaymentService();
    String result = service.releasePayment(2);
    
    assertTrue(result.startsWith("HOLD"));
    assertTrue(result.contains("REJECTED"));
}
```

### Integration Test Example
```java
@Test
public void testPaymentReleaseUpdatesMilestone() {
    // 1. Create milestone
    // 2. Create approved inspection
    // 3. Create high rating feedback
    // 4. Release payment
    // 5. Verify milestone status = PAID
    // 6. Verify payment status = RELEASED
}
```

---

## Dependency Diagram

```
PaymentService
├── Uses: InspectionDAO (check latest inspection)
├── Uses: FeedbackDAO (calculate average rating)
├── Uses: PaymentDAO (update payment status)
├── Uses: MilestoneDAO (update milestone status)
└── Uses: DBConnection (manage transactions)

ProjectService
├── Uses: ProjectDAO
└── Uses: UserDAO (validate contractor exists)
```
