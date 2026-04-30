# Data Access Object (DAO) Layer Documentation

## Overview

The DAO (Data Access Object) layer provides a bridge between the application logic and the database. Each DAO class encapsulates database operations for a specific entity, implementing CRUD (Create, Read, Update, Delete) operations.

**Location**: `src/dao/`

**Pattern**: DAO Pattern - Isolates database logic from business logic

## DAO Classes

### 1. UserDAO

Handles user authentication and user lookups.

```java
public class UserDAO {
    public User login(String email, String password)
    public List<User> getAllContractors()
    public List<User> getAllInspectors()
}
```

#### Methods

##### login(email, password)
```java
public User login(String email, String password)
```

**Purpose**: Authenticate user with email and password

**Parameters**:
- `email` (String): User's email address
- `password` (String): User's password (plain-text ⚠️)

**Returns**:
- `User`: User object if credentials match
- `null`: If no matching user found

**SQL Query**:
```sql
SELECT * FROM users WHERE email = ? AND password = ?
```

**Usage Example**:
```java
UserDAO dao = new UserDAO();
User user = dao.login("admin@pwqpvs.gov", "admin123");
if (user != null) {
    System.out.println("Login successful: " + user.getName());
} else {
    System.out.println("Invalid credentials");
}
```

**Security Note**: ⚠️ Passwords stored and compared as plain text. Should use hashing in production.

---

##### getAllContractors()
```java
public List<User> getAllContractors()
```

**Purpose**: Fetch all contractors for dropdown selections

**Parameters**: None

**Returns**:
- `List<User>`: All users with role = 'CONTRACTOR'
- Empty list if no contractors found

**SQL Query**:
```sql
SELECT * FROM users WHERE role = 'CONTRACTOR'
```

**Usage Example**:
```java
UserDAO dao = new UserDAO();
List<User> contractors = dao.getAllContractors();
for (User contractor : contractors) {
    System.out.println(contractor.getName());  // Display name in dropdown
}
```

**Use Cases**:
- Populating contractor dropdown in AdminDashboard when creating a project
- Filtering projects by contractor

---

##### getAllInspectors()
```java
public List<User> getAllInspectors()
```

**Purpose**: Fetch all inspectors for assignments

**Parameters**: None

**Returns**:
- `List<User>`: All users with role = 'INSPECTOR'
- Empty list if no inspectors found

**SQL Query**:
```sql
SELECT * FROM users WHERE role = 'INSPECTOR'
```

**Usage Example**:
```java
UserDAO dao = new UserDAO();
List<User> inspectors = dao.getAllInspectors();
// Use for assigning inspectors to milestones
```

---

### 2. ProjectDAO

Manages project creation, retrieval, and status updates.

```java
public class ProjectDAO {
    public boolean addProject(String name, String location, int contractorId, double budget)
    public List<Project> getAllProjects()
    public List<Project> getProjectsByContractor(int contractorId)
    public boolean updateStatus(int projectId, String status)
}
```

#### Methods

##### addProject(name, location, contractorId, budget)
```java
public boolean addProject(String name, String location, int contractorId, double budget)
```

**Purpose**: Create a new project (Admin only)

**Parameters**:
- `name` (String): Project name
- `location` (String): Project location
- `contractorId` (int): User ID of assigned contractor
- `budget` (double): Total project budget

**Returns**:
- `true`: Project created successfully
- `false`: Project creation failed

**SQL Query**:
```sql
INSERT INTO projects (name, location, contractor_id, total_budget) 
VALUES (?, ?, ?, ?)
```

**Usage Example**:
```java
ProjectDAO dao = new ProjectDAO();
boolean success = dao.addProject(
    "Highway Expansion",
    "New Delhi",
    2,           // contractor_id
    50000.00
);
if (success) {
    System.out.println("Project created");
} else {
    System.out.println("Project creation failed");
}
```

---

##### getAllProjects()
```java
public List<Project> getAllProjects()
```

**Purpose**: Fetch all projects (Admin view)

**Parameters**: None

**Returns**:
- `List<Project>`: All projects ordered by creation date (newest first)

**SQL Query**:
```sql
SELECT * FROM projects ORDER BY created_at DESC
```

**Usage Example**:
```java
ProjectDAO dao = new ProjectDAO();
List<Project> allProjects = dao.getAllProjects();
for (Project p : allProjects) {
    System.out.println(p);  // Display in table
}
```

---

##### getProjectsByContractor(contractorId)
```java
public List<Project> getProjectsByContractor(int contractorId)
```

**Purpose**: Get all projects assigned to a specific contractor

**Parameters**:
- `contractorId` (int): User ID of contractor

**Returns**:
- `List<Project>`: Projects assigned to the contractor

**SQL Query**:
```sql
SELECT * FROM projects WHERE contractor_id = ?
```

**Usage Example**:
```java
ProjectDAO dao = new ProjectDAO();
List<Project> myProjects = dao.getProjectsByContractor(2);  // Contractor ID 2
for (Project p : myProjects) {
    System.out.println(p);  // Display in contractor dashboard
}
```

---

##### updateStatus(projectId, status)
```java
public boolean updateStatus(int projectId, String status)
```

**Purpose**: Update project status (e.g., ACTIVE → COMPLETED)

**Parameters**:
- `projectId` (int): Project to update
- `status` (String): New status (ACTIVE, COMPLETED, SUSPENDED)

**Returns**:
- `true`: Status updated successfully
- `false`: Update failed

**SQL Query**:
```sql
UPDATE projects SET status = ? WHERE project_id = ?
```

**Usage Example**:
```java
ProjectDAO dao = new ProjectDAO();
boolean success = dao.updateStatus(1, "COMPLETED");
if (success) {
    System.out.println("Project marked as completed");
}
```

---

### 3. MilestoneDAO

**Status**: Interface defined, Implementation: `MilestoneDAOImpl`

Manages milestone CRUD operations.

**Expected Methods**:
```java
public boolean addMilestone(int projectId, String description, double amount)
public List<Milestone> getMilestonesByProject(int projectId)
public Milestone getMilestoneById(int milestoneId)
public boolean updateStatus(int milestoneId, String status)
```

---

### 4. InspectionDAO

**Status**: Interface defined, Implementation: `InspectionDAOImpl`

Manages inspection records.

**Expected Methods**:
```java
public boolean addInspection(int milestoneId, int inspectorId, String result, String remarks)
public Inspection getLatestInspection(int milestoneId)
public List<Inspection> getInspectionsByInspector(int inspectorId)
```

---

### 5. PaymentDAO

**Status**: Interface defined, Implementation: `PaymentDAOImpl`

Manages payment records.

**Expected Methods**:
```java
public boolean addPayment(int milestoneId, double amount)
public Payment getPaymentByMilestone(int milestoneId)
public boolean updateStatus(int paymentId, String status)
public List<Payment> getAllPayments()
```

---

### 6. FeedbackDAO

**Status**: Interface defined, Implementation: `FeedbackDAOImpl`

Manages public feedback.

**Expected Methods**:
```java
public boolean addFeedback(int projectId, int rating, String comment)
public List<PublicFeedback> getFeedbackByProject(int projectId)
public double getAverageRating(int projectId)
```

---

## Database Connection Management

### DBConnection Utility

Located in `src/util/DBConnection.java`:

```java
public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/pwqpvs_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Roshan25";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

**Connection Pattern**:
```java
try (Connection conn = DBConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    // Execute query
} catch (SQLException e) {
    System.out.println("Error: " + e.getMessage());
}
```

**Try-with-resources**: Automatically closes connection

---

## SQL Injection Prevention

All DAOs use **Parameterized Queries** ✅

### Safe (Parameterized):
```java
String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, email);        // Values inserted safely
ps.setString(2, password);
```

### Unsafe (String Concatenation):
```java
String sql = "SELECT * FROM users WHERE email = '" + email + "'";  // ❌ SQL Injection risk
```

---

## Error Handling Pattern

### Standard Error Handling in DAOs

```java
try {
    // Database operation
} catch (SQLException e) {
    System.out.println("Operation error: " + e.getMessage());
    return null;  // or false, or empty list
}
```

**Current State**: 
- Errors logged to console
- Null/false/empty returned on failure

**Future Improvement**: 
- Custom exception classes
- Proper logging framework
- Meaningful error messages to UI

---

## DAO Usage Examples

### Example 1: User Login Flow
```java
// In LoginScreen.java
UserDAO userDAO = new UserDAO();
User user = userDAO.login(emailInput, passwordInput);

if (user != null) {
    // Route to appropriate dashboard
    switch (user.getRole()) {
        case "ADMIN":
            new AdminDashboard(user).setVisible(true);
            break;
        case "CONTRACTOR":
            new ContractorDashboard(user).setVisible(true);
            break;
        // ...
    }
} else {
    JOptionPane.showMessageDialog(this, "Invalid credentials");
}
```

### Example 2: Create Project Flow
```java
// In AdminDashboard.java
ProjectDAO projectDAO = new ProjectDAO();
boolean success = projectDAO.addProject(
    projectName,
    location,
    selectedContractorId,
    budget
);

if (success) {
    JOptionPane.showMessageDialog(this, "Project created");
    refreshProjectList();
} else {
    JOptionPane.showMessageDialog(this, "Failed to create project");
}
```

### Example 3: Get Contractor Projects
```java
// In ContractorDashboard.java
ProjectDAO projectDAO = new ProjectDAO();
List<Project> myProjects = projectDAO.getProjectsByContractor(currentUser.getUserId());

// Display in table
DefaultTableModel model = (DefaultTableModel) projectTable.getModel();
for (Project p : myProjects) {
    model.addRow(new Object[]{
        p.getProjectId(),
        p.getName(),
        p.getLocation(),
        p.getTotalBudget(),
        p.getStatus()
    });
}
```

---

## Best Practices

### ✅ DO

1. **Use Try-with-Resources**
   ```java
   try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
       // Code
   }
   ```

2. **Use Parameterized Queries**
   ```java
   ps.setString(1, email);     // Safe
   ps.setInt(2, userId);       // Type-safe
   ```

3. **Check Results**
   ```java
   if (rs.next()) {
       // Process result
   }
   ```

4. **Handle Exceptions**
   ```java
   catch (SQLException e) {
       System.out.println("Error: " + e.getMessage());
       return null;
   }
   ```

### ❌ DON'T

1. **Don't concatenate strings into SQL**
   ```java
   String sql = "SELECT * FROM users WHERE id = " + userId;  // ❌ Wrong
   ```

2. **Don't leave connections open**
   ```java
   Connection conn = DriverManager.getConnection(...);  // ❌ No close()
   ```

3. **Don't ignore exceptions silently**
   ```java
   catch (SQLException e) {}  // ❌ Silent failure
   ```

4. **Don't hardcode credentials in DAO**
   ```java
   // ⚠️ Consider: Environment variables, properties files, config servers
   ```

---

## Testing DAOs

### Mock Testing Example
```java
@Test
public void testLogin() {
    UserDAO dao = new UserDAO();
    User user = dao.login("admin@pwqpvs.gov", "admin123");
    
    assertNotNull(user);
    assertEquals("Admin Ravi", user.getName());
    assertEquals("ADMIN", user.getRole());
}
```

---

## Performance Considerations

### Current State
- No connection pooling (new connection per request)
- No query optimization
- No caching

### Future Improvements
1. **Connection Pooling** (HikariCP, C3P0)
   ```java
   HikariConfig config = new HikariConfig();
   config.setMaximumPoolSize(10);
   HikariDataSource ds = new HikariDataSource(config);
   ```

2. **Query Optimization**
   ```sql
   -- Add indexes
   CREATE INDEX idx_projects_contractor_id ON projects(contractor_id);
   ```

3. **Pagination** for large result sets
   ```sql
   SELECT * FROM projects LIMIT 10 OFFSET 0;
   ```

4. **Lazy Loading** for related entities

---

## DAO Hierarchy

```
UserDAO
├── login()
├── getAllContractors()
└── getAllInspectors()

ProjectDAO
├── addProject()
├── getAllProjects()
├── getProjectsByContractor()
└── updateStatus()

MilestoneDAO (via MilestoneDAOImpl)
├── addMilestone()
├── getMilestonesByProject()
├── getMilestoneById()
└── updateStatus()

InspectionDAO (via InspectionDAOImpl)
├── addInspection()
├── getLatestInspection()
└── getInspectionsByInspector()

PaymentDAO (via PaymentDAOImpl)
├── addPayment()
├── getPaymentByMilestone()
├── updateStatus()
└── getAllPayments()

FeedbackDAO (via FeedbackDAOImpl)
├── addFeedback()
├── getFeedbackByProject()
└── getAverageRating()
```
