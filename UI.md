# User Interface (UI) Components Documentation

## Overview

The UI layer provides the desktop interface for the Nirikshan application using Java Swing. Each user role has a specialized dashboard with relevant functionality.

**Location**: `src/ui/`

**Framework**: Swing (GUI toolkit)

## UI Components

### 1. LoginScreen

Entry point for the application. Authenticates users and routes them to appropriate dashboards.

```java
public class LoginScreen extends JFrame {
    // Components: Email input, Password input, Login button
    // Validates credentials via UserDAO
    // Routes to appropriate dashboard based on role
}
```

**Responsibility**:
- Display login form
- Validate user credentials
- Authenticate against database
- Route to correct dashboard

**User Flow**:
```
1. User enters email and password
2. Click "Login" button
3. Validate with UserDAO.login(email, password)
4. If valid:
   - Create appropriate dashboard (Admin/Contractor/Inspector/Public)
   - Display dashboard
   - Close login screen
5. If invalid:
   - Show error message
   - Clear password field
   - Request retry
```

**Credentials**:
| Email | Password | Role |
|-------|----------|------|
| admin@pwqpvs.gov | admin123 | ADMIN |
| suresh@builds.com | pass123 | CONTRACTOR |
| meera@inspect.gov | inspect123 | INSPECTOR |
| arjun@citizen.com | pub123 | PUBLIC |

**Security Notes**: ⚠️ Credentials transmitted as plain text. Use HTTPS in production.

---

### 2. AdminDashboard

Admin interface for managing projects, users, and payments.

```java
public class AdminDashboard extends JFrame {
    // Features:
    // - Create new projects
    // - Assign contractors
    // - Assign inspectors to milestones
    // - View all projects
    // - Manage payment releases
    // - Monitor system-wide status
}
```

**Admin Capabilities**:

| Feature | Description |
|---------|-------------|
| Create Project | Create new construction project with budget |
| Assign Contractor | Select contractor from dropdown |
| View All Projects | See status of all projects in system |
| Release Payments | Trigger payment authorization process |
| Manage Milestones | Create and manage project milestones |
| Assign Inspectors | Assign inspectors to specific milestones |

**Typical Workflow**:
```
1. Create Project
   - Enter: Name, Location, Budget
   - Select: Contractor
   - Save to database
   ↓
2. Add Milestones
   - Enter: Description, Amount
   - Multiple milestones per project
   ↓
3. Assign Inspector
   - Select: Milestone, Inspector
   ↓
4. Approve/Release Payment
   - System checks: Inspection + Rating
   - Approve if conditions met
```

---

### 3. ContractorDashboard

Interface for contractors to manage their projects and submit milestones.

```java
public class ContractorDashboard extends JFrame {
    // Features:
    // - View assigned projects
    // - Submit milestones for inspection
    // - Track milestone status
    // - Monitor payment status
    // - View project budget vs actual
}
```

**Contractor Capabilities**:

| Feature | Description |
|---------|-------------|
| View Projects | See projects assigned to them |
| Submit Milestone | Mark milestone as ready for inspection |
| Track Progress | View status of all their milestones |
| Check Payments | See payment status (HOLD/RELEASED) |
| View Feedback | Check public ratings on projects |

**Typical Workflow**:
```
1. View my projects (ProjectDAO.getProjectsByContractor)
   ↓
2. Select project and view milestones
   ↓
3. Submit milestone for inspection
   - Status: PENDING → SUBMITTED
   ↓
4. Wait for inspector to inspect
   ↓
5. Check payment status after inspection
   - If RELEASED: Payment transferred
   - If HOLD: Review feedback/inspection comments
```

---

### 4. InspectorDashboard

Interface for inspectors to conduct quality checks on milestones.

```java
public class InspectorDashboard extends JFrame {
    // Features:
    // - View pending inspections
    // - Inspect milestones
    // - Approve/Reject/Request rework
    // - Add inspection remarks
    // - View inspection history
}
```

**Inspector Capabilities**:

| Feature | Description |
|---------|-------------|
| View Pending Milestones | See milestones awaiting inspection |
| Conduct Inspection | Review work quality |
| Approve/Reject | Make quality determination |
| Request Rework | Mark as NEEDS_REWORK |
| Add Remarks | Document observations |

**Typical Workflow**:
```
1. View pending milestones (submitted for inspection)
   ↓
2. Inspect the work
   ↓
3. Make decision:
   - APPROVED: Work meets quality standards ✅
   - REJECTED: Work doesn't meet standards ❌
   - NEEDS_REWORK: Minor issues to fix ⚙️
   ↓
4. Add remarks/comments
   ↓
5. Submit inspection
   - InspectionDAO.addInspection()
   - If APPROVED: Payment eligible for release
```

**Inspection Result Consequences**:

| Result | Effect on Payment | Next Step |
|--------|------------------|-----------|
| APPROVED | Eligible (if rating ≥ 3.0) | Payment can be released |
| REJECTED | Not eligible | Milestone marked REJECTED, no payment |
| NEEDS_REWORK | Not eligible | Contractor redoes work, reinspects |

---

### 5. PublicViewScreen

Public interface for viewing projects and submitting feedback.

```java
public class PublicViewScreen extends JFrame {
    // Features:
    // - Browse all active projects
    // - View project details
    // - Submit rating (1-5 stars)
    // - Leave feedback comments
    // - View other feedback
}
```

**Public User Capabilities**:

| Feature | Description |
|---------|-------------|
| Browse Projects | See all active projects |
| View Details | Project location, budget, status |
| Rate Project | Submit 1-5 star rating |
| Leave Comment | Provide feedback text |
| View Ratings | See average rating and comments |

**Impact on System**:
```
Public Feedback
    ↓
Average Rating
    ↓
Used in Payment Release Decision
(Must be ≥ 3.0 for payment approval)
```

**Typical Workflow**:
```
1. View active projects
   ↓
2. Select project
   ↓
3. Review project details
   ↓
4. Submit feedback:
   - Select rating (1-5 stars)
   - Optional: Add comment
   - Submit
   ↓
5. Feedback saved to database
   ↓
6. Affects payment decisions
```

**Public Feedback Impact**:
- **Low Average Rating (< 3.0)**: Prevents payment release even if inspection approved
- **High Average Rating (≥ 3.0)**: Enables payment release (with inspection approval)
- **No Feedback Yet**: Can delay payment until sufficient feedback received

---

## Custom Swing Components

### 1. JButtonStyled

Custom styled button component with enhanced appearance.

```java
public class JButtonStyled extends JButton {
    // Custom styling:
    // - Color customization
    // - Font styling
    // - Hover effects
    // - Border styling
}
```

**Purpose**: Provide consistent, professional button appearance across application

---

### 2. RoundedPanel

Custom panel with rounded corners for modern appearance.

```java
public class RoundedPanel extends JPanel {
    // Features:
    // - Rounded corner radius
    // - Background color
    // - Border color and width
    // - Anti-aliasing for smooth rendering
}
```

**Purpose**: Create modern, polished UI sections with rounded aesthetics

---

### 3. NavBar

Navigation bar component for dashboard navigation.

```java
public class NavBar extends JPanel {
    // Features:
    // - Current user display
    // - Navigation buttons
    // - Logout button
    // - Dashboard-specific options
}
```

**Purpose**: Consistent navigation across all dashboards

---

### 4. Theme

Application-wide theme and styling configuration.

```java
public class Theme {
    // Color definitions
    // Font definitions
    // Styling constants
    // Theme switching logic
}
```

**Purpose**: Centralized styling for consistent look and feel

---

## UI Architectural Patterns

### 1. Role-Based Dashboard Routing

```java
// In LoginScreen
User user = userDAO.login(email, password);
switch (user.getRole()) {
    case "ADMIN":
        new AdminDashboard(user).setVisible(true);
        break;
    case "CONTRACTOR":
        new ContractorDashboard(user).setVisible(true);
        break;
    case "INSPECTOR":
        new InspectorDashboard(user).setVisible(true);
        break;
    case "PUBLIC":
        new PublicViewScreen(user).setVisible(true);
        break;
}
this.dispose();  // Close login screen
```

### 2. Dashboard Pattern

Each dashboard follows similar structure:

```java
public class [Role]Dashboard extends JFrame {
    private User currentUser;
    private [Role]DAO dao;
    
    public [Role]Dashboard(User user) {
        this.currentUser = user;
        this.dao = new [Role]DAO();
        initializeUI();
        loadData();
    }
    
    private void initializeUI() {
        // Create components
        // Set layouts
        // Add listeners
    }
    
    private void loadData() {
        // Load from database
        // Display in tables/lists
    }
    
    private void refresh() {
        // Reload data
        // Update display
    }
}
```

### 3. Table Model Pattern

Display database records in JTable:

```java
DefaultTableModel model = new DefaultTableModel();
model.addColumn("Project ID");
model.addColumn("Name");
model.addColumn("Location");

List<Project> projects = projectDAO.getAllProjects();
for (Project p : projects) {
    model.addRow(new Object[]{
        p.getProjectId(),
        p.getName(),
        p.getLocation()
    });
}

JTable table = new JTable(model);
JScrollPane scrollPane = new JScrollPane(table);
```

---

## UI Data Flow

### Example: Admin Releasing Payment

```
AdminDashboard
    │
    ├─ User clicks "Release Payment"
    │
    ├─ Get selected milestone from table
    │
    ├─ Call PaymentService.releasePayment(milestoneId)
    │
    ├─ Service checks:
    │  ├─ InspectionDAO.getLatestInspection()
    │  ├─ FeedbackDAO.getAverageRating()
    │  └─ Database transaction (update payment + milestone)
    │
    ├─ Service returns result string
    │
    ├─ Parse result status
    │
    ├─ Display message via JOptionPane
    │
    └─ Refresh payment table
```

---

## Common UI Tasks

### Displaying a List of Records

```java
// In dashboard constructor or refresh method
List<Project> projects = projectDAO.getAllProjects();

DefaultTableModel model = (DefaultTableModel) table.getModel();
model.setRowCount(0);  // Clear existing rows

for (Project p : projects) {
    model.addRow(new Object[]{
        p.getProjectId(),
        p.getName(),
        p.getLocation(),
        String.format("₹%.2f", p.getTotalBudget()),
        p.getStatus()
    });
}
```

### Getting User Selection from Table

```java
int selectedRow = table.getSelectedRow();
if (selectedRow == -1) {
    JOptionPane.showMessageDialog(this, "Please select a row");
    return;
}

int id = (int) table.getValueAt(selectedRow, 0);  // First column = ID
String name = (String) table.getValueAt(selectedRow, 1);
```

### Creating Input Dialog

```java
String projectName = JOptionPane.showInputDialog(this, 
    "Enter project name:", 
    "Create Project", 
    JOptionPane.QUESTION_MESSAGE);

if (projectName != null && !projectName.isEmpty()) {
    // Process input
}
```

### Displaying Error Message

```java
JOptionPane.showMessageDialog(this, 
    "Invalid data. Please check your inputs.", 
    "Error", 
    JOptionPane.ERROR_MESSAGE);
```

### Displaying Success Message

```java
JOptionPane.showMessageDialog(this, 
    "Payment released successfully!", 
    "Success", 
    JOptionPane.INFORMATION_MESSAGE);
```

---

## UI Best Practices

### ✅ DO

1. **Refresh data after modifications**
   ```java
   projectDAO.addProject(...);
   refreshProjectList();  // Update table
   ```

2. **Validate input before processing**
   ```java
   if (name.isEmpty() || budget <= 0) {
       JOptionPane.showMessageDialog(...);
       return;
   }
   ```

3. **Use meaningful component names**
   ```java
   JButton btnReleasePayment;  // Clear purpose
   JLabel lblProjectStatus;
   ```

4. **Provide feedback for user actions**
   ```java
   JOptionPane.showMessageDialog(this, "Project created successfully");
   ```

5. **Disable buttons during processing**
   ```java
   btnReleasePayment.setEnabled(false);
   // Process...
   btnReleasePayment.setEnabled(true);
   ```

### ❌ DON'T

1. **Don't block UI thread with database calls**
   ```java
   // ❌ Wrong - freezes UI
   List<Project> projects = getAllProjects();  // Long operation
   
   // ✅ Right - use SwingWorker
   new SwingWorker<List<Project>, Void>() {
       protected List<Project> doInBackground() {
           return getAllProjects();
       }
   }.execute();
   ```

2. **Don't ignore exceptions**
   ```java
   // ❌ Wrong
   try {
       dao.getProjects();
   } catch (Exception e) {}
   
   // ✅ Right
   try {
       dao.getProjects();
   } catch (SQLException e) {
       JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
   }
   ```

3. **Don't hardcode values**
   ```java
   // ❌ Wrong
   Font font = new Font("Arial", Font.PLAIN, 12);
   
   // ✅ Right - use Theme class
   Font font = Theme.BODY_FONT;
   ```

4. **Don't create new DAOs repeatedly**
   ```java
   // ❌ Wrong - inefficient
   new ProjectDAO().getAllProjects();
   new ProjectDAO().addProject(...);
   
   // ✅ Right
   ProjectDAO dao = new ProjectDAO();
   dao.getAllProjects();
   dao.addProject(...);
   ```

---

## UI Enhancements (Future)

1. **Search and Filter**
   - Filter projects by status
   - Search projects by name or location
   - Filter milestones by status

2. **Pagination**
   - Handle large result sets efficiently
   - Display 10-20 rows per page

3. **Export Functionality**
   - Export project data to CSV
   - Export payment reports to PDF

4. **Real-time Notifications**
   - Notify inspectors of pending inspections
   - Alert contractors of payment status
   - Notify admin of low ratings

5. **Dashboard Redesign**
   - Add charts and graphs
   - Display key metrics (total budget, payments released, etc.)
   - Show project progress visually

6. **Multi-threading**
   - Load data in background using SwingWorker
   - Prevent UI freezing during database operations

7. **Better Error Handling**
   - Catch and display database connection errors
   - Retry mechanisms for failed operations

8. **Accessibility**
   - Support for screen readers
   - Keyboard navigation
   - High contrast mode
