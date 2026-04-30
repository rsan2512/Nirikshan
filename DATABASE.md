# Database Schema Documentation

## Overview

Nirikshan uses PostgreSQL as its database management system. The schema consists of 6 main tables with relationships designed to support the construction project payment workflow.

## Database: `pwqpvs_db`

### Connection Details
- **Host**: localhost
- **Port**: 5432 (default)
- **Database**: pwqpvs_db
- **User**: postgres
- **Password**: (set in DBConnection.java)

## Table Definitions

### 1. `users`

Stores all system users with their authentication details and roles.

```sql
CREATE TABLE users (
    user_id     SERIAL PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(100)    UNIQUE NOT NULL,
    password    VARCHAR(100)    NOT NULL,
    role        VARCHAR(20)     NOT NULL 
                CHECK (role IN ('ADMIN','CONTRACTOR','INSPECTOR','PUBLIC')),
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);
```

**Columns**:
| Column | Type | Constraint | Description |
|--------|------|-----------|-------------|
| user_id | SERIAL | PRIMARY KEY | Unique identifier |
| name | VARCHAR(100) | NOT NULL | User's full name |
| email | VARCHAR(100) | UNIQUE, NOT NULL | Email address (login credential) |
| password | VARCHAR(100) | NOT NULL | Password (plain-text ⚠️) |
| role | VARCHAR(20) | CHECK, NOT NULL | User role: ADMIN, CONTRACTOR, INSPECTOR, PUBLIC |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation date |

**Indexes**: 
- PRIMARY KEY on user_id
- UNIQUE on email

**Sample Data**:
```
admin@pwqpvs.gov       → ADMIN
suresh@builds.com      → CONTRACTOR
meera@inspect.gov      → INSPECTOR
arjun@citizen.com      → PUBLIC
```

---

### 2. `projects`

Represents construction projects managed by contractors.

```sql
CREATE TABLE projects (
    project_id      SERIAL PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL,
    location        VARCHAR(200)    NOT NULL,
    contractor_id   INT             NOT NULL REFERENCES users(user_id),
    total_budget    NUMERIC(15,2)   NOT NULL,
    status          VARCHAR(20)     DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE','COMPLETED','SUSPENDED')),
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);
```

**Columns**:
| Column | Type | Constraint | Description |
|--------|------|-----------|-------------|
| project_id | SERIAL | PRIMARY KEY | Unique identifier |
| name | VARCHAR(200) | NOT NULL | Project name |
| location | VARCHAR(200) | NOT NULL | Project location/address |
| contractor_id | INT | FOREIGN KEY (users) | Assigned contractor |
| total_budget | NUMERIC(15,2) | NOT NULL | Total project budget |
| status | VARCHAR(20) | CHECK, DEFAULT | ACTIVE, COMPLETED, or SUSPENDED |
| created_at | TIMESTAMP | DEFAULT | Project creation date |

**Relationships**:
- FOREIGN KEY (contractor_id) → users(user_id)

**Status Values**:
- **ACTIVE**: Project is ongoing
- **COMPLETED**: Project finished
- **SUSPENDED**: Project paused

---

### 3. `milestones`

Project milestones with individual budgets and approval workflow.

```sql
CREATE TABLE milestones (
    milestone_id    SERIAL PRIMARY KEY,
    project_id      INT             NOT NULL REFERENCES projects(project_id),
    description     VARCHAR(500)    NOT NULL,
    amount          NUMERIC(15,2)   NOT NULL,
    status          VARCHAR(20)     DEFAULT 'PENDING'
                    CHECK (status IN 
                    ('PENDING','SUBMITTED','APPROVED','REJECTED','PAID'))
);
```

**Columns**:
| Column | Type | Constraint | Description |
|--------|------|-----------|-------------|
| milestone_id | SERIAL | PRIMARY KEY | Unique identifier |
| project_id | INT | FOREIGN KEY (projects) | Parent project |
| description | VARCHAR(500) | NOT NULL | Work description |
| amount | NUMERIC(15,2) | NOT NULL | Milestone budget |
| status | VARCHAR(20) | CHECK, DEFAULT | Workflow status |

**Relationships**:
- FOREIGN KEY (project_id) → projects(project_id)

**Status Workflow**:
```
PENDING → SUBMITTED → APPROVED → PAID
                   ↘ REJECTED
```

- **PENDING**: Created but not submitted
- **SUBMITTED**: Contractor submitted for inspection
- **APPROVED**: Inspector approved the work
- **REJECTED**: Inspector rejected the work
- **PAID**: Payment released to contractor

---

### 4. `inspections`

Quality inspection records for milestones.

```sql
CREATE TABLE inspections (
    inspection_id   SERIAL PRIMARY KEY,
    milestone_id    INT             NOT NULL REFERENCES milestones(milestone_id),
    inspector_id    INT             NOT NULL REFERENCES users(user_id),
    result          VARCHAR(20)     NOT NULL
                    CHECK (result IN ('APPROVED','REJECTED','NEEDS_REWORK')),
    remarks         TEXT,
    inspected_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);
```

**Columns**:
| Column | Type | Constraint | Description |
|--------|------|-----------|-------------|
| inspection_id | SERIAL | PRIMARY KEY | Unique identifier |
| milestone_id | INT | FOREIGN KEY (milestones) | Inspected milestone |
| inspector_id | INT | FOREIGN KEY (users) | Inspector who conducted inspection |
| result | VARCHAR(20) | CHECK, NOT NULL | APPROVED, REJECTED, or NEEDS_REWORK |
| remarks | TEXT | | Inspector comments |
| inspected_at | TIMESTAMP | DEFAULT | Inspection timestamp |

**Relationships**:
- FOREIGN KEY (milestone_id) → milestones(milestone_id)
- FOREIGN KEY (inspector_id) → users(user_id)

**Result Values**:
- **APPROVED**: Work meets quality standards ✅
- **REJECTED**: Work doesn't meet standards ❌
- **NEEDS_REWORK**: Work needs modifications ⚙️

---

### 5. `payments`

Payment records tied to milestones.

```sql
CREATE TABLE payments (
    payment_id      SERIAL PRIMARY KEY,
    milestone_id    INT             NOT NULL REFERENCES milestones(milestone_id),
    amount          NUMERIC(15,2)   NOT NULL,
    status          VARCHAR(20)     DEFAULT 'HOLD'
                    CHECK (status IN ('HOLD','RELEASED','REJECTED')),
    released_at     TIMESTAMP
);
```

**Columns**:
| Column | Type | Constraint | Description |
|--------|------|-----------|-------------|
| payment_id | SERIAL | PRIMARY KEY | Unique identifier |
| milestone_id | INT | FOREIGN KEY (milestones) | Payment for which milestone |
| amount | NUMERIC(15,2) | NOT NULL | Payment amount |
| status | VARCHAR(20) | CHECK, DEFAULT | HOLD, RELEASED, or REJECTED |
| released_at | TIMESTAMP | | Payment release timestamp |

**Relationships**:
- FOREIGN KEY (milestone_id) → milestones(milestone_id)

**Status Values**:
- **HOLD**: Payment pending approval (initial state)
- **RELEASED**: Payment authorized ✅
- **REJECTED**: Payment denied ❌

**Payment Release Rules** (in PaymentService):
```
IF (latest_inspection == APPROVED) AND (avg_public_rating >= 3.0)
    THEN status = RELEASED
    ELSE status = HOLD
```

---

### 6. `public_feedback`

Public ratings and comments on projects.

```sql
CREATE TABLE public_feedback (
    feedback_id     SERIAL PRIMARY KEY,
    project_id      INT             NOT NULL REFERENCES projects(project_id),
    rating          INT             NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    submitted_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);
```

**Columns**:
| Column | Type | Constraint | Description |
|--------|------|-----------|-------------|
| feedback_id | SERIAL | PRIMARY KEY | Unique identifier |
| project_id | INT | FOREIGN KEY (projects) | Project being rated |
| rating | INT | CHECK (1-5), NOT NULL | Star rating (1-5) |
| comment | TEXT | | Optional feedback text |
| submitted_at | TIMESTAMP | DEFAULT | Submission timestamp |

**Relationships**:
- FOREIGN KEY (project_id) → projects(project_id)

**Rating Scale**:
| Stars | Meaning |
|-------|---------|
| 1 ⭐ | Very Poor |
| 2 ⭐⭐ | Poor |
| 3 ⭐⭐⭐ | Average |
| 4 ⭐⭐⭐⭐ | Good |
| 5 ⭐⭐⭐⭐⭐ | Excellent |

---

## Entity Relationship Diagram

```
┌─────────────┐
│   users     │
├─────────────┤
│ user_id (PK)│
│ name        │
│ email       │
│ password    │
│ role        │
└──────┬──────┘
       │
       │ contractor_id
       │
┌──────▼──────────────┐
│   projects          │
├─────────────────────┤
│ project_id (PK)     │
│ name                │
│ location            │
│ contractor_id (FK)  │
│ total_budget        │
│ status              │
└──────┬──────────────┘
       │
       │ project_id
       │
┌──────▼──────────────────┐
│   milestones            │
├─────────────────────────┤
│ milestone_id (PK)       │
│ project_id (FK)         │
│ description             │
│ amount                  │
│ status                  │
└────┬─────────────┬──────┘
     │             │
     │ milestone_id│ project_id
     │             │
     │    ┌────────▼─────────┐
     │    │ public_feedback  │
     │    ├──────────────────┤
     │    │ feedback_id (PK) │
     │    │ project_id (FK)  │
     │    │ rating (1-5)     │
     │    │ comment          │
     │    └──────────────────┘
     │
     ├──────────────────────────────────┐
     │                                  │
┌────▼─────────────┐        ┌──────────▼──────────┐
│   inspections    │        │   payments          │
├──────────────────┤        ├─────────────────────┤
│ inspection_id(PK)│        │ payment_id (PK)     │
│ milestone_id(FK) │        │ milestone_id (FK)   │
│ inspector_id(FK) │        │ amount              │
│ result           │        │ status              │
│ remarks          │        │ released_at         │
└────┬─────────────┘        └─────────────────────┘
     │
     │ inspector_id
     │
     └──────────────────→ users(user_id)
```

## Indexes

### Existing Indexes
- PRIMARY KEY on all tables (automatically indexed)
- UNIQUE on users.email

### Recommended Additional Indexes
```sql
-- For faster project lookups
CREATE INDEX idx_projects_contractor_id ON projects(contractor_id);

-- For faster milestone lookups
CREATE INDEX idx_milestones_project_id ON milestones(project_id);

-- For faster inspection lookups
CREATE INDEX idx_inspections_milestone_id ON inspections(milestone_id);
CREATE INDEX idx_inspections_inspector_id ON inspections(inspector_id);

-- For faster payment lookups
CREATE INDEX idx_payments_milestone_id ON payments(milestone_id);

-- For faster feedback lookups
CREATE INDEX idx_feedback_project_id ON public_feedback(project_id);
```

## Sample Queries

### Get all projects by a contractor
```sql
SELECT * FROM projects 
WHERE contractor_id = 2 
ORDER BY created_at DESC;
```

### Get milestone details with inspection results
```sql
SELECT 
    m.milestone_id,
    m.description,
    m.amount,
    m.status,
    i.result,
    i.remarks
FROM milestones m
LEFT JOIN inspections i ON m.milestone_id = i.milestone_id
WHERE m.project_id = 1
ORDER BY m.milestone_id;
```

### Get average rating for a project
```sql
SELECT 
    project_id,
    ROUND(AVG(rating)::numeric, 1) as avg_rating,
    COUNT(*) as total_reviews
FROM public_feedback
WHERE project_id = 1
GROUP BY project_id;
```

### Get payment status for all milestones
```sql
SELECT 
    m.milestone_id,
    m.description,
    m.amount,
    p.status,
    p.released_at
FROM milestones m
LEFT JOIN payments p ON m.milestone_id = p.milestone_id
ORDER BY m.milestone_id;
```

## Database Maintenance

### Backup
```bash
pg_dump -U postgres pwqpvs_db > backup.sql
```

### Restore
```bash
psql -U postgres pwqpvs_db < backup.sql
```

### Check Database Size
```sql
SELECT 
    datname as database_name,
    pg_size_pretty(pg_database_size(datname)) as size
FROM pg_database
WHERE datname = 'pwqpvs_db';
```

## Data Integrity Rules

### Constraints
1. **NOT NULL**: Critical fields cannot be empty
2. **UNIQUE**: Email addresses must be unique (prevent duplicate accounts)
3. **FOREIGN KEY**: Referential integrity maintained
4. **CHECK**: Valid values enforced (e.g., role IN ('ADMIN', 'CONTRACTOR', ...))

### Cascade Rules
- **On Delete User**: Projects and inspections become orphaned ⚠️
- **On Delete Project**: Milestones, payments, feedback deleted
- **On Delete Milestone**: Inspections and payments deleted

**Recommendation**: Add CASCADE DELETE rules to database or implement application-level cascading.
