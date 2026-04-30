# System Architecture

## Overview

Nirikshan follows a **layered architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         UI Layer (Swing)                │
│  (Dashboards, Forms, Screens)           │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│         Service Layer                   │
│  (Business Logic, Payment Rules)        │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│         DAO Layer                       │
│  (Database Access, CRUD Operations)     │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│         Database (PostgreSQL)           │
│  (Persistent Data Storage)              │
└─────────────────────────────────────────┘
```

## Architecture Layers

### 1. UI Layer (Presentation)

**Location**: `src/ui/`

Handles all user interaction through Swing components:
- **LoginScreen**: Entry point, user authentication
- **AdminDashboard**: Project and user management
- **ContractorDashboard**: Contractor's project view
- **InspectorDashboard**: Inspection task management
- **PublicViewScreen**: Public project viewing & feedback
- **NavBar**: Navigation component
- **Custom Components**: Styled buttons, rounded panels, custom themes

**Responsibility**:
- Display data to users
- Collect user input
- Call service/DAO methods
- Route to appropriate screens based on user role

### 2. Service Layer (Business Logic)

**Location**: `src/service/`

Implements complex business rules and workflows:

#### PaymentService
- **releasePayment(milestoneId)**: Core logic for payment authorization
- Enforces business rules (inspection approval, rating threshold)
- Manages database transactions
- Returns detailed status messages

#### ProjectService
- Project creation and status updates
- Project retrieval by various criteria

**Responsibility**:
- Implement business rules
- Manage transactions
- Coordinate between multiple DAOs
- Return meaningful results to UI

### 3. DAO Layer (Data Access)

**Location**: `src/dao/`

Each entity has a corresponding DAO class:

| DAO Class | Responsibility |
|-----------|-----------------|
| **UserDAO** | User authentication, fetch contractors/inspectors |
| **ProjectDAO** | CRUD for projects, filter by contractor |
| **MilestoneDAO** | Milestone management |
| **InspectionDAO** | Inspection records |
| **PaymentDAO** | Payment records |
| **FeedbackDAO** | Public feedback management |

**Responsibility**:
- Execute SQL queries
- Map ResultSets to model objects
- Handle database errors
- Provide clean method interface to services/UI

### 4. Model Layer (Data Objects)

**Location**: `src/model/`

Plain Java objects representing database entities:
- `User`: System user with role
- `Project`: Construction project
- `Milestone`: Project milestone with budget
- `Inspection`: Quality inspection record
- `Payment`: Payment transaction
- `PublicFeedback`: User ratings and comments

**Responsibility**:
- Store data in memory
- Provide getters (mostly immutable)
- Simple toString() for display

### 5. Database Layer

**Technology**: PostgreSQL

Provides persistent storage with:
- 6 core tables (users, projects, milestones, inspections, payments, public_feedback)
- Foreign key relationships
- Check constraints for data validation
- Timestamps for audit trail

**Responsibility**:
- Persistent data storage
- ACID compliance
- Referential integrity
- Query execution

## Key Design Patterns

### 1. DAO Pattern

Encapsulates data access logic:
```
UI/Service → DAO → SQL → Database
```

Advantages:
- Isolates database logic
- Easy to test with mock DAOs
- Simple to switch databases

### 2. Service Layer Pattern

Centralizes business logic:
```
UI → Service → DAO → Database
```

Advantages:
- Separates UI from business rules
- Reusable across different UIs
- Easy to test business logic independently

### 3. Factory Pattern (Implicit)

Different dashboards based on user role:
- Admin → AdminDashboard
- Contractor → ContractorDashboard
- Inspector → InspectorDashboard
- Public → PublicViewScreen

### 4. Singleton Pattern (Database Connection)

`DBConnection.getConnection()` provides consistent database access.

## Data Flow

### Example: Release Payment Flow

```
1. Inspector clicks "Approve Milestone" in InspectorDashboard
           ↓
2. UI calls PaymentService.releasePayment(milestoneId)
           ↓
3. Service loads inspection result via InspectionDAO
           ↓
4. Service loads average rating via FeedbackDAO
           ↓
5. Service checks: approved? YES. rating >= 3? YES.
           ↓
6. Service starts transaction
           ↓
7. Service updates payment via PaymentDAO → RELEASED
           ↓
8. Service updates milestone via MilestoneDAO → PAID
           ↓
9. Service commits transaction
           ↓
10. Service returns "RELEASED|Payment successfully released..."
           ↓
11. UI displays success message
```

## Connection Pooling Note

Currently, each DAO request creates a new database connection:
```java
Connection conn = DBConnection.getConnection();
// use connection
conn.close();
```

**Future Improvement**: Implement connection pooling (HikariCP, C3P0) for better performance under load.

## Transaction Management

`PaymentService` demonstrates proper transaction handling:

```java
conn.setAutoCommit(false);  // START TRANSACTION

try {
    // Multiple operations
    updatePayment(...);
    updateMilestone(...);
    conn.commit();          // SUCCESS
} catch (SQLException e) {
    conn.rollback();        // FAILURE - undo all
}
```

Ensures consistency: Either ALL updates succeed or NONE do.

## Error Handling

### Strategy
- SQL exceptions caught and logged
- Errors returned via result strings
- UI displays user-friendly messages

### Example (PaymentService)
```
"RELEASED|Payment successfully released..."  // Success
"HOLD|Inspection status is REJECTED..."      // Business rule failed
"ERROR|Connection timeout..."                // Database error
```

## Performance Considerations

### Current State
- No query optimization
- No caching
- Full table scans possible
- N+1 query problem possible

### Future Improvements
- Add indexes on foreign keys
- Implement pagination for large result sets
- Cache frequently accessed data
- Use prepared statements for all queries ✅ (already done)
- Consider query joining instead of multiple queries

## Security Architecture

### Current Implementation
- ⚠️ Plain-text password storage
- ⚠️ Hardcoded database credentials
- ⚠️ No input validation on client side
- ✅ SQL injection protection (parameterized queries)

### Recommended Improvements
- Implement password hashing (bcrypt, PBKDF2)
- Use environment variables for credentials
- Add input validation and sanitization
- Implement role-based authorization checks
- Add audit logging for sensitive operations
- Use HTTPS for remote database connections
- Implement session management
