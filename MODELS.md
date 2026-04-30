# Data Models Documentation

## Overview

Models are simple Java objects (POJOs - Plain Old Java Objects) representing entities in the system. Each model corresponds to a database table and is used to transfer data between the database layer and business logic.

**Location**: `src/model/`

## Model Classes

### 1. User

Represents a system user with authentication and role information.

```java
public class User {
    private int    userId;
    private String name;
    private String email;
    private String password;
    private String role;
}
```

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| userId | int | Unique identifier (auto-incremented) |
| name | String | User's full name |
| email | String | Email address (login credential) |
| password | String | Password for authentication |
| role | String | User role: ADMIN, CONTRACTOR, INSPECTOR, PUBLIC |

**Constructors**:
```java
User(int userId, String name, String email, String password, String role)
```

**Accessors**:
```java
getUserId()    → int
getName()      → String
getEmail()     → String
getPassword()  → String
getRole()      → String
```

**toString()**:
```
"John Doe [ADMIN]"
```

**Usage Example**:
```java
User admin = new User(1, "Admin Ravi", "admin@pwqpvs.gov", "admin123", "ADMIN");
String roleDisplay = admin.toString(); // "Admin Ravi [ADMIN]"
```

**Role Types**:
- **ADMIN**: System administrator, manages projects and users
- **CONTRACTOR**: Construction contractor, submits milestones
- **INSPECTOR**: Quality inspector, approves/rejects work
- **PUBLIC**: Public user, views projects and submits feedback

---

### 2. Project

Represents a construction project.

```java
public class Project {
    private int    projectId;
    private String name;
    private String location;
    private int    contractorId;
    private double totalBudget;
    private String status;
}
```

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| projectId | int | Unique identifier (auto-incremented) |
| name | String | Project name |
| location | String | Project location/address |
| contractorId | int | User ID of assigned contractor |
| totalBudget | double | Total project budget (currency) |
| status | String | Project status: ACTIVE, COMPLETED, SUSPENDED |

**Constructors**:
```java
Project(int projectId, String name, String location, 
        int contractorId, double totalBudget, String status)
```

**Accessors**:
```java
getProjectId()    → int
getName()         → String
getLocation()     → String
getContractorId() → int
getTotalBudget()  → double
getStatus()       → String
```

**toString()**:
```
"Highway Expansion — New Delhi"
```

**Status Values**:
- **ACTIVE**: Project is ongoing
- **COMPLETED**: Project finished successfully
- **SUSPENDED**: Project paused (on hold)

**Usage Example**:
```java
Project project = new Project(1, "Highway Expansion", "New Delhi", 2, 50000.00, "ACTIVE");
System.out.println(project);  // "Highway Expansion — New Delhi"
```

---

### 3. Milestone

Represents a milestone within a project with budget and approval workflow.

```java
public class Milestone {
    private int    milestoneId;
    private int    projectId;
    private String description;
    private double amount;
    private String status;
}
```

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| milestoneId | int | Unique identifier (auto-incremented) |
| projectId | int | Parent project ID |
| description | String | Work description (what needs to be done) |
| amount | double | Budget for this milestone |
| status | String | Workflow status |

**Constructors**:
```java
// Empty constructor for flexibility
Milestone()

// Full constructor with all fields
Milestone(int milestoneId, int projectId, String description, 
          double amount, String status)
```

**Accessors**:
```java
getMilestoneId()  → int
setMilestoneId()
getProjectId()    → int
setProjectId()
getDescription()  → String
setDescription()
getBudget()       → double  // Note: returns 'amount' field
setBudget()
getStatus()       → String
setStatus()
```

**Status Workflow**:
```
PENDING → SUBMITTED → APPROVED → PAID
                   ↘ REJECTED
```

- **PENDING**: Created but not submitted
- **SUBMITTED**: Contractor submitted for inspection
- **APPROVED**: Inspector approved the work
- **REJECTED**: Inspector rejected, needs rework
- **PAID**: Payment released successfully

**Usage Example**:
```java
Milestone milestone = new Milestone();
milestone.setMilestoneId(1);
milestone.setProjectId(1);
milestone.setDescription("Foundation laying");
milestone.setBudget(10000.00);
milestone.setStatus("PENDING");
```

---

### 4. Inspection

Represents a quality inspection record for a milestone.

```java
public class Inspection {
    private int    inspectionId;
    private int    milestoneId;
    private int    inspectorId;
    private String result;
    private String remarks;
}
```

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| inspectionId | int | Unique identifier (auto-incremented) |
| milestoneId | int | Milestone being inspected |
| inspectorId | int | User ID of inspector |
| result | String | Inspection result: APPROVED, REJECTED, NEEDS_REWORK |
| remarks | String | Inspector comments and observations |

**Constructors**:
```java
Inspection(int inspectionId, int milestoneId, int inspectorId, 
           String result, String remarks)
```

**Accessors**:
```java
getInspectionId() → int
getMilestoneId()  → int
getInspectorId()  → int
getResult()       → String
getRemarks()      → String
```

**toString()**:
```
"Inspection #5 → APPROVED"
```

**Result Values**:
- **APPROVED**: Work meets quality standards ✅
- **REJECTED**: Work doesn't meet standards ❌
- **NEEDS_REWORK**: Work needs modifications ⚙️

**Usage Example**:
```java
Inspection inspection = new Inspection(
    5, 
    1,                    // milestoneId
    3,                    // inspectorId (Meera)
    "APPROVED",
    "Foundation is properly laid. Concrete strength adequate."
);
System.out.println(inspection);  // "Inspection #5 → APPROVED"
```

**Important**: Latest inspection result determines payment eligibility. Latest APPROVED inspection is required for payment release.

---

### 5. Payment

Represents a payment record tied to a milestone.

```java
public class Payment {
    private int    paymentId;
    private int    milestoneId;
    private double amount;
    private String status;
}
```

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| paymentId | int | Unique identifier (auto-incremented) |
| milestoneId | int | Milestone for which payment is due |
| amount | double | Payment amount |
| status | String | Payment status: HOLD, RELEASED, REJECTED |

**Constructors**:
```java
Payment(int paymentId, int milestoneId, double amount, String status)
```

**Accessors**:
```java
getPaymentId()   → int
getMilestoneId() → int
getAmount()      → double
getStatus()      → String
```

**toString()**:
```
"Payment #7 ₹50000 [HOLD]"
```

**Status Values**:
- **HOLD**: Payment pending (initial state)
- **RELEASED**: Payment authorized and can be transferred ✅
- **REJECTED**: Payment denied ❌

**Business Rules for Payment Release**:
```
1. Latest inspection must be APPROVED
2. Average public rating must be ≥ 3.0/5 stars

IF both conditions pass:
    status = RELEASED
    milestone.status = PAID
ELSE:
    status = HOLD
```

**Usage Example**:
```java
Payment payment = new Payment(7, 1, 50000.00, "HOLD");
System.out.println(payment);  // "Payment #7 ₹50000 [HOLD]"
```

---

### 6. PublicFeedback

Represents public user ratings and comments on projects.

```java
public class PublicFeedback {
    private int    feedbackId;
    private int    projectId;
    private int    rating;
    private String comment;
}
```

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| feedbackId | int | Unique identifier (auto-incremented) |
| projectId | int | Project being rated |
| rating | int | Star rating (1-5) |
| comment | String | Optional text comment |

**Constructors**:
```java
PublicFeedback(int feedbackId, int projectId, int rating, String comment)
```

**Accessors**:
```java
getFeedbackId() → int
getProjectId()  → int
getRating()     → int
getComment()    → String
```

**toString()**:
```
"Feedback #12 ⭐4"
```

**Rating Scale**:
| Stars | Meaning |
|-------|---------|
| 1 ⭐ | Very Poor |
| 2 ⭐⭐ | Poor |
| 3 ⭐⭐⭐ | Average |
| 4 ⭐⭐⭐⭐ | Good |
| 5 ⭐⭐⭐⭐⭐ | Excellent |

**Impact on Payments**:
- Average feedback rating affects payment release decisions
- Minimum 3.0 average rating required for payment authorization
- Low ratings can prevent payment release (with inspection failure)

**Usage Example**:
```java
PublicFeedback feedback = new PublicFeedback(
    12,
    1,
    4,
    "Great project quality. Completed on time!"
);
System.out.println(feedback);  // "Feedback #12 ⭐4"
```

---

## Model Design Patterns

### 1. Immutability (Mostly)

Models are mostly immutable with only getters:
```java
User user = new User(1, "John", "john@example.com", "pass123", "ADMIN");
// No setters - data cannot be modified after creation
```

**Exception**: `Milestone` class has setters for flexibility during object construction.

### 2. toString() for Display

Each model provides meaningful string representation:
```java
user.toString()      // "John Doe [ADMIN]"
project.toString()   // "Highway Expansion — New Delhi"
payment.toString()   // "Payment #7 ₹50000 [HOLD]"
```

### 3. Value Objects

Models are simple data containers without complex logic. Business logic is in Service/DAO layer.

## Model Usage in Application

### 1. Creating Models from Database

```java
// In DAO classes
ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE user_id = 1");
if (rs.next()) {
    User user = new User(
        rs.getInt("user_id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("password"),
        rs.getString("role")
    );
}
```

### 2. Passing Models Between Layers

```java
// UI → Service → DAO
User user = new User(...);              // Create in UI
paymentService.releasePayment(user);    // Pass to service
userDAO.getUserById(user.getUserId());  // Use in DAO
```

### 3. Displaying Models in UI

```java
// Display in JTable or JLabel
project p = projectList.get(0);
tableModel.setValueAt(p.getName(), row, 0);
tableModel.setValueAt(p.getLocation(), row, 1);
```

## Best Practices

### ✅ DO

- Use models as data containers
- Keep models simple and focused
- Use getters to access model data
- Create new model instances rather than modifying existing ones

### ❌ DON'T

- Add business logic to models
- Use model instances across multiple network calls
- Forget to handle null checks when displaying models
- Modify model fields directly (breaks encapsulation)

## Future Improvements

1. **Validation**: Add input validation in constructors
   ```java
   if (rating < 1 || rating > 5) {
       throw new IllegalArgumentException("Rating must be 1-5");
   }
   ```

2. **Serialization**: Add `Serializable` interface for persistence
   ```java
   public class User implements Serializable {
       private static final long serialVersionUID = 1L;
   }
   ```

3. **Equals & HashCode**: Override for proper object comparison
   ```java
   @Override
   public boolean equals(Object o) { ... }
   
   @Override
   public int hashCode() { ... }
   ```

4. **Immutability**: Use `final` keyword, remove setters
   ```java
   public class Milestone {
       private final int milestoneId;
       private final String description;
   }
   ```
