# Project Flow Documentation

## System Flow Overview

The Nirikshan system orchestrates a construction project payment workflow involving four key roles and multiple verification steps to ensure payments are only released when quality standards are met.

---

## 1. User Authentication Flow

### Entry Point: Login Screen

```
┌─────────────────────────────────────┐
│     LOGIN SCREEN                    │
├─────────────────────────────────────┤
│ Email input field                   │
│ Password input field                │
│ [Login] button                      │
└────────────┬────────────────────────┘
             │
             │ User enters credentials
             │
             ▼
┌─────────────────────────────────────┐
│   VALIDATE CREDENTIALS              │
│ UserDAO.login(email, password)      │
├─────────────────────────────────────┤
│ Query: SELECT * FROM users          │
│   WHERE email = ? AND password = ?  │
└────────────┬──────────┬─────────────┘
             │          │
        ✅ FOUND    ❌ NOT FOUND
             │          │
             ▼          ▼
      ┌─────────────┐  ┌──────────────────┐
      │ Get User    │  │ Show error:      │
      │ object      │  │ "Invalid        │
      │             │  │  credentials"   │
      └─────┬───────┘  │ Clear password  │
            │          │ Retry login     │
            │          └──────────────────┘
            │
            ▼
    ┌──────────────────────┐
    │ CHECK USER ROLE      │
    └──┬──┬──┬──┬──────────┘
       │  │  │  │
    ADMIN CONTRACTOR INSPECTOR PUBLIC
       │  │  │  │
       ▼  ▼  ▼  ▼
    ┌─────────────┐  ┌──────────────────┐  ┌────────────────┐  ┌──────────────────┐
    │ AdminDash   │  │ ContractorDash   │  │ InspectorDash  │  │ PublicViewScreen │
    │ - All       │  │ - My projects    │  │ - Inspect      │  │ - View projects  │
    │   projects  │  │ - My milestones  │  │ - Approve/     │  │ - Rate projects  │
    │ - Create    │  │ - Submit work    │  │   Reject work  │  │ - Leave feedback │
    │   project   │  │ - Track payment  │  │ - Add remarks  │  │                  │
    │ - Release   │  │                  │  │                │  │                  │
    │   payment   │  │                  │  │                │  │                  │
    └─────────────┘  └──────────────────┘  └────────────────┘  └──────────────────┘
```

**Key Points**:
- Email is unique and required for login
- Password compared as plain text (security concern ⚠️)
- Each role sees different dashboards
- No other authentication (no 2FA, sessions, etc.)

---

## 2. Project Lifecycle Flow

### Complete Project Journey

```
                    ┌─────────────────────────────────────┐
                    │  ADMIN DASHBOARD                    │
                    │  Create New Project                 │
                    └────────────┬────────────────────────┘
                                 │
                                 │ Admin enters:
                                 │ - Project name
                                 │ - Location
                                 │ - Total budget
                                 │ - Select contractor
                                 │
                                 ▼
                    ┌─────────────────────────────────────┐
                    │  CREATE PROJECT                     │
                    │ ProjectDAO.addProject()             │
                    ├─────────────────────────────────────┤
                    │ INSERT INTO projects:               │
                    │ name, location, contractor_id,      │
                    │ total_budget, status='ACTIVE'       │
                    └────────────┬────────────────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────────────────┐
                    │  PROJECT CREATED                    │
                    │  Status: ACTIVE                     │
                    │  ✓ ID assigned                      │
                    └────────────┬────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌──────────────────────┐   ┌──────────────────────┐
        │ ADD MILESTONES       │   │ CONTRACTOR VIEWS     │
        │ Admin creates        │   │ Contractor sees      │
        │ milestone steps      │   │ project in their     │
        │ with budgets         │   │ dashboard            │
        └──────────┬───────────┘   └──────────────────────┘
                   │
                   │ Multiple milestones
                   │ (e.g., Foundation, Walls, Roof)
                   │
                   ▼
        ┌──────────────────────┐
        │ MILESTONES READY     │
        │ All status: PENDING  │
        └──────────┬───────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ Contractor submits   │
        │ milestone for        │
        │ inspection           │
        │ Status: SUBMITTED    │
        └──────────┬───────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ Inspector inspects   │
        │ milestone work       │
        │ (see Quality Flow)   │
        └──────────┬───────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
    APPROVED           NOT APPROVED
        │                     │
        ▼                     ▼
    ┌──────────┐     ┌──────────────────┐
    │ Ready for│     │ Marked REJECTED  │
    │ payment  │     │ or NEEDS_REWORK  │
    │ release  │     │ Contractor fixes │
    └────┬─────┘     └──────────────────┘
         │
         ▼
    ┌──────────────────────┐
    │ Admin releases       │
    │ payment (if rating   │
    │ conditions met)      │
    │ (see Payment Flow)   │
    └────┬─────────────────┘
         │
         ▼
    ┌──────────────────────┐
    │ Payment RELEASED     │
    │ Contractor paid      │
    │ Milestone: PAID      │
    └────┬─────────────────┘
         │
    All milestones paid?
         │
    ┌────┴─────────┐
    │              │
   NO             YES
    │              │
    │              ▼
    │          ┌──────────────┐
    │          │ Update project│
    │          │ status:       │
    │          │ COMPLETED     │
    │          └──────────────┘
    │
    └──→ Next milestone ready
         for inspection
```

---

## 3. Milestone Submission Flow

### Contractor Submitting Work for Inspection

```
CONTRACTOR DASHBOARD
        │
        ├─ View my projects
        │  (ProjectDAO.getProjectsByContractor)
        │
        ├─ Select project
        │
        └─ View milestones
           (Status: PENDING, SUBMITTED, APPROVED, REJECTED, PAID)
                 │
                 ▼
        ┌─────────────────────────────┐
        │ PENDING milestone           │
        │ - Description: "Foundation" │
        │ - Amount: ₹10,000           │
        │ - Status: PENDING           │
        └────────┬────────────────────┘
                 │
                 │ Contractor clicks
                 │ "Submit for Inspection"
                 │
                 ▼
        ┌─────────────────────────────┐
        │ UPDATE MILESTONE STATUS     │
        │ MilestoneDAO.updateStatus() │
        ├─────────────────────────────┤
        │ UPDATE milestones           │
        │ SET status = 'SUBMITTED'    │
        │ WHERE milestone_id = ?      │
        └────────┬────────────────────┘
                 │
                 ▼
        ┌─────────────────────────────┐
        │ SUBMITTED                   │
        │ Now visible to Inspector    │
        └─────────────────────────────┘
```

---

## 4. Quality Assurance / Inspection Flow

### Inspector Inspecting and Approving Work

```
INSPECTOR DASHBOARD
        │
        ├─ View pending inspections
        │  (Milestones with status = SUBMITTED)
        │
        ├─ Select milestone to inspect
        │
        └─ View details:
           - Description
           - Amount
           - Contractor name
           - Project location
                 │
                 ▼
        ┌─────────────────────────────┐
        │ CONDUCT INSPECTION          │
        │ Inspector reviews work      │
        │ quality on-site             │
        └────────┬────────────────────┘
                 │
    ┌────────────┼────────────────┐
    │            │                │
    ▼            ▼                ▼
APPROVED    REJECTED      NEEDS_REWORK
    │            │                │
    │            │                │
    ▼            ▼                ▼
┌────────────┐ ┌──────────┐  ┌──────────────┐
│Work meets  │ │Work not  │  │Work has      │
│standards ✅│ │adequate  │  │minor issues  │
│            │ │❌        │  │⚙️             │
└─────┬──────┘ └────┬─────┘  └──────┬───────┘
      │             │               │
      │             │               │
      ▼             ▼               ▼
  Inspector adds remarks (observations, issues, etc.)
      │             │               │
      │             │               │
      ▼             ▼               ▼
┌──────────────────────────────────────┐
│ InspectionDAO.addInspection()        │
├──────────────────────────────────────┤
│ INSERT INTO inspections:             │
│ - milestone_id                       │
│ - inspector_id                       │
│ - result (APPROVED/REJECTED/...)     │
│ - remarks (comments)                 │
│ - inspected_at (timestamp)           │
└──────────────┬───────────────────────┘
               │
               ▼
    ┌──────────────────────────────────┐
    │ INSPECTION RECORDED              │
    │ Status saved in database         │
    └──────────────┬───────────────────┘
                   │
         ┌─────────┴──────────┐
         │                    │
    APPROVED            NOT APPROVED
    (REJECTED or        (NEEDS_REWORK)
     NEEDS_REWORK)
         │                    │
         ▼                    ▼
    ┌──────────────┐    ┌──────────────┐
    │Milestone     │    │Contractor    │
    │marked        │    │notified to   │
    │accordingly   │    │fix issues    │
    │              │    │              │
    │Status:       │    │Resubmit when │
    │APPROVED/     │    │ready         │
    │REJECTED      │    │              │
    └──────────────┘    └──────────────┘
         │
         ▼
    ┌──────────────────────────────────┐
    │ MILESTONE INSPECTION COMPLETE    │
    │ Ready for next step              │
    │ (Payment release or rework)      │
    └──────────────────────────────────┘
```

---

## 5. Payment Release Flow (Core Business Logic)

### The Critical Payment Authorization Workflow

```
ADMIN DASHBOARD
        │
        ├─ View payments (Status: HOLD)
        │
        ├─ Select milestone payment
        │
        └─ Click "Release Payment"
                 │
                 ▼
        ┌──────────────────────────────────┐
        │ PaymentService.releasePayment()  │
        │ START TRANSACTION                │
        └────────┬───────────────────────────┘
                 │
                 ▼
        ┌──────────────────────────────────┐
        │ RULE 1: CHECK INSPECTION         │
        │ Get latest inspection for        │
        │ this milestone                   │
        │                                  │
        │ SELECT result FROM inspections   │
        │ WHERE milestone_id = ?           │
        │ ORDER BY inspected_at DESC       │
        │ LIMIT 1                          │
        └────────┬───────────────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
NO INSPECTION          INSPECTION FOUND
    │                         │
    ▼                         ▼
ERROR                  ┌──────────────┐
│                      │ Check result │
│                      └──┬───────┬───┘
│                         │       │
│                    APPROVED   OTHER
│                         │       │
│                         │       ▼
│                         │    ERROR
│                         │    │
│                         │    ▼
│                         │  ┌──────────────────────┐
│                         │  │ ROLLBACK             │
│                         │  │ Payment stays HOLD   │
│                         │  │ Return: "HOLD|       │
│                         │  │ Inspection result    │
│                         │  │ must be APPROVED"    │
│                         │  └──────────────────────┘
│                         │
│                         ▼
│                    ✅ APPROVED
│                         │
│                         ▼
│            ┌─────────────────────────────┐
│            │ RULE 2: CHECK PUBLIC RATING │
│            │ Calculate average rating    │
│            │                             │
│            │ SELECT AVG(rating)          │
│            │ FROM public_feedback        │
│            │ WHERE project_id = ?        │
│            └────────┬────────────────────┘
│                     │
│          ┌──────────┴──────────┐
│          │                     │
│      Rating < 3.0          Rating >= 3.0
│          │                     │
│          ▼                     ▼
│   ┌─────────────┐         ┌───────────┐
│   │ ROLLBACK    │         │ ✅ Both   │
│   │ Payment     │         │ rules pass│
│   │ stays HOLD  │         │           │
│   │ Return:     │         └─────┬─────┘
│   │ "HOLD|      │               │
│   │ Public      │               ▼
│   │ rating is   │    ┌──────────────────────┐
│   │ X.X/5"      │    │ UPDATE PAYMENT       │
│   └─────────────┘    │ SET status=RELEASED  │
│                      │ WHERE milestone_id=? │
│                      └──────┬───────────────┘
│                             │
│                             ▼
│                    ┌──────────────────────┐
│                    │ UPDATE MILESTONE     │
│                    │ SET status='PAID'    │
│                    │ WHERE milestone_id=? │
│                    └──────┬───────────────┘
│                           │
│                           ▼
│                    ┌──────────────────────┐
│                    │ COMMIT TRANSACTION   │
│                    │ ✅ Both updates done │
│                    └──────┬───────────────┘
│                           │
└────────────────────────────┼──────────────────┐
                             │                  │
                             ▼                  ▼
                    ┌──────────────────┐  ┌──────────────┐
                    │ SUCCESS          │  │ PAYMENT      │
                    │ Payment RELEASED │  │ STILL HELD   │
                    │ Amount: ₹10,000  │  │ Reason shown │
                    │ Return message:  │  │ to admin     │
                    │ "RELEASED|..."   │  └──────────────┘
                    └──────────────────┘
```

**Payment Release Decision Logic**:
```
IF (latest_inspection.result == APPROVED)
AND (avg_public_rating >= 3.0)
THEN:
    - Payment status = RELEASED
    - Milestone status = PAID
    - COMMIT all changes
    - Return SUCCESS
ELSE:
    - ROLLBACK all changes
    - Payment stays HOLD
    - Return REASON
```

**Key Examples**:

| Scenario | Inspection | Rating | Result |
|----------|-----------|--------|--------|
| Perfect | APPROVED | 4.5/5 | ✅ RELEASED |
| Failed inspection | REJECTED | 4.5/5 | ❌ HOLD |
| Low rating | APPROVED | 2.1/5 | ❌ HOLD |
| Both fail | REJECTED | 2.1/5 | ❌ HOLD |
| No inspection | NONE | 4.5/5 | ❌ HOLD |
| No rating yet | APPROVED | 0 | ❌ HOLD |

---

## 6. Public Feedback Collection Flow

### How Public Ratings Affect System

```
PUBLIC USER (Citizen)
        │
        ├─ Browse active projects
        │  (ProjectDAO.getAllProjects)
        │
        ├─ View project details:
        │  - Name: "Highway Expansion"
        │  - Location: "New Delhi"
        │  - Contractor: "Suresh"
        │  - Budget: ₹50,000
        │  - Current rating: 3.8/5
        │
        └─ Submit feedback
                 │
                 ▼
        ┌──────────────────────────────────┐
        │ SELECT RATING (1-5 stars)        │
        │ ⭐ (1=Very Poor)                  │
        │ ⭐⭐ (2=Poor)                     │
        │ ⭐⭐⭐ (3=Average)                │
        │ ⭐⭐⭐⭐ (4=Good)                 │
        │ ⭐⭐⭐⭐⭐ (5=Excellent)          │
        └────────┬─────────────────────────┘
                 │
                 ▼
        ┌──────────────────────────────────┐
        │ OPTIONAL: ADD COMMENT             │
        │ "Great project quality!"          │
        │                                  │
        │ [Submit Feedback]                │
        └────────┬─────────────────────────┘
                 │
                 ▼
        ┌──────────────────────────────────┐
        │ FeedbackDAO.addFeedback()        │
        ├──────────────────────────────────┤
        │ INSERT INTO public_feedback:     │
        │ - project_id                     │
        │ - rating (1-5)                   │
        │ - comment (optional)             │
        │ - submitted_at (now)             │
        └────────┬─────────────────────────┘
                 │
                 ▼
        ┌──────────────────────────────────┐
        │ FEEDBACK RECORDED                │
        └────────┬─────────────────────────┘
                 │
    ┌────────────┴─────────────────────────────┐
    │                                          │
    ▼                                          ▼
CALCULATE AVERAGE RATING            IMMEDIATE IMPACT
    │                                          │
    ▼                                          │
SELECT AVG(rating)                      If payment pending:
FROM public_feedback                     │
WHERE project_id = ?                     ├─ Rating >= 3.0?
    │                                    │  Payment can release
    ▼                                    │
Current: 3.2/5                          └─ Rating < 3.0?
(e.g., 16 reviews)                         Payment held
    │
    ▼
This rating used in:
PaymentService.releasePayment()

New feedback → Rating updated → 
Payment eligibility checked
```

**Rating Impact Chain**:
```
Public submits feedback
         │
         ▼
Average rating changes
         │
         ▼
Used in payment decision
         │
    ┌────┴────┐
    │          │
Rating>=3.0  Rating<3.0
    │          │
    │          └─→ Prevents payment release
    │
    └─→ Payment can proceed (if inspection approved)
```

---

## 7. Complete End-to-End Flow

### From Project Creation to Payment Release

```
STEP 1: ADMIN CREATES PROJECT
    ├─ Project name: "Highway Expansion"
    ├─ Location: "New Delhi"
    ├─ Contractor: Suresh (ID: 2)
    ├─ Budget: ₹50,000
    ├─ Status: ACTIVE
    └─ Created: 2026-04-30
             │
             ▼
STEP 2: ADMIN ADDS MILESTONES
    ├─ M1: Foundation - ₹10,000 (PENDING)
    ├─ M2: Walls - ₹15,000 (PENDING)
    └─ M3: Roof - ₹25,000 (PENDING)
             │
             ▼
STEP 3: CONTRACTOR VIEWS PROJECT
    ├─ Sees project in dashboard
    ├─ Reviews milestones
    └─ Works on Foundation
             │
             ▼
STEP 4: CONTRACTOR SUBMITS M1
    ├─ Clicks "Submit for Inspection"
    ├─ Status: PENDING → SUBMITTED
    ├─ Admin assigns Inspector
    └─ Inspector gets notification
             │
             ▼
STEP 5: INSPECTOR INSPECTS M1
    ├─ Reviews work quality
    ├─ Checks foundation strength
    ├─ Approves: "Foundation is solid"
    ├─ Status: SUBMITTED → APPROVED
    └─ Inspection recorded in DB
             │
             ▼
STEP 6: PUBLIC RATES PROJECT
    ├─ User visits project page
    ├─ Submits 5-star rating
    ├─ Adds comment: "Excellent work"
    ├─ Average rating: 4.5/5 (from 3 reviews)
    └─ Feedback stored in DB
             │
             ▼
STEP 7: ADMIN RELEASES PAYMENT
    ├─ Clicks "Release Payment" for M1
    ├─ System checks:
    │  ├─ Inspection = APPROVED ✅
    │  ├─ Rating = 4.5/5 (>= 3.0) ✅
    │  └─ Both pass
    ├─ System updates:
    │  ├─ Payment status → RELEASED
    │  ├─ Milestone status → PAID
    │  └─ released_at timestamp → NOW
    ├─ Transaction committed
    └─ Admin sees: "Payment released"
             │
             ▼
STEP 8: CONTRACTOR PAID
    ├─ Payment amount: ₹10,000
    ├─ Status: RELEASED
    ├─ Ready for bank transfer
    └─ Contractor notified
             │
             ▼
STEP 9: REPEAT FOR M2 & M3
    ├─ Contractor works on next milestone
    ├─ Submits when ready
    ├─ Inspector reviews
    ├─ Public rates (if first hasn't started)
    ├─ Admin releases payment
    └─ Repeat process
             │
             ▼
STEP 10: PROJECT COMPLETION
    ├─ All 3 milestones: PAID
    ├─ Total paid: ₹50,000
    ├─ Project status: COMPLETED
    ├─ Final average rating: 4.2/5
    └─ Project closed
```

---

## 8. Data Flow Between Components

### Request/Response Cycle

```
┌──────────────┐
│   UI LAYER   │ (LoginScreen, AdminDashboard, etc.)
├──────────────┤
│ - Display UI │
│ - Get input  │
│ - Show msgs  │
└───────┬──────┘
        │ Calls DAO/Service
        │ (e.g., releasePayment(5))
        │
        ▼
┌──────────────┐
│SERVICE LAYER │ (PaymentService, ProjectService)
├──────────────┤
│ - Check rules│
│ - Coordinate│
│   DAOs       │
│ - Manage TX  │
└───────┬──────┘
        │ Calls multiple DAOs
        │
        ▼
┌──────────────┐
│  DAO LAYER   │ (UserDAO, ProjectDAO, PaymentDAO, etc.)
├──────────────┤
│ - Execute    │
│   SQL        │
│ - Map to     │
│   Models     │
└───────┬──────┘
        │ Executes SQL statements
        │
        ▼
┌──────────────┐
│  DATABASE    │ (PostgreSQL)
├──────────────┤
│ - Store data │
│ - Return     │
│   results    │
└───────┬──────┘
        │ ResultSet/Status
        │
        ▼
┌──────────────┐
│  DAO LAYER   │ Returns Model objects
├──────────────┤
│ Constructs   │
│ Model from   │
│ ResultSet    │
└───────┬──────┘
        │ Returns Model
        │ (e.g., Payment object)
        │
        ▼
┌──────────────┐
│SERVICE LAYER │ Returns result string
├──────────────┤
│ Processes    │ "RELEASED|Payment released..."
│ and formats  │ or
│ result       │ "HOLD|Rating below threshold..."
└───────┬──────┘
        │
        ▼
┌──────────────┐
│   UI LAYER   │ Displays result to user
├──────────────┤
│ Parse status │
│ Show message │
│ Refresh data │
└──────────────┘
```

---

## 9. Transaction Flow (Complex Operations)

### Payment Release with Atomicity

```
Admin clicks "Release Payment"
        │
        ▼
START TRANSACTION (conn.setAutoCommit(false))
        │
        ├─ Point A: If error here → ROLLBACK
        │
        ├─ Execute: Check inspection result
        │   └─ SQL: SELECT FROM inspections
        │       └─ If not APPROVED → ROLLBACK
        │
        ├─ Point B: If error here → ROLLBACK
        │
        ├─ Execute: Check average rating
        │   └─ SQL: SELECT AVG(rating) FROM feedback
        │       └─ If < 3.0 → ROLLBACK
        │
        ├─ Point C: All checks passed
        │
        ├─ Execute: Update payment
        │   └─ SQL: UPDATE payments SET status='RELEASED'
        │       └─ If error → ROLLBACK
        │
        ├─ Execute: Update milestone
        │   └─ SQL: UPDATE milestones SET status='PAID'
        │       └─ If error → ROLLBACK
        │
        ▼
COMMIT TRANSACTION (conn.commit())
        │
    ┌───┴────┐
    │        │
SUCCESS   FAILURE
    │        │
✅OK      ❌Rolled back
Payment   All changes
released  undone
Milestone
marked PAID
```

**Transaction Safety Guarantee**:
- If ANY operation fails → ALL changes rolled back
- Database never left in inconsistent state
- Either ALL updates succeed or NONE do

---

## 10. Error Handling Flow

### How Errors Are Handled

```
Operation in any layer (UI, Service, DAO)
        │
        ▼
    Try block
        │
    ┌───┴──────────────┐
    │                  │
 SUCCESS            EXCEPTION
    │                  │
    │                  ▼
    │            Catch SQLEception
    │                  │
    │                  ├─ Rollback transaction
    │                  │
    │                  ├─ Log error
    │                  │
    │                  ▼
    │            Return error message
    │            (usually null/false/error string)
    │                  │
    ▼                  ▼
Return to UI
    │
    ├─ Success case: Update display
    │
    └─ Error case: Show error message
       to user
```

**Example**: Payment Release Errors
```
1. No inspection found
   → Return "HOLD|No inspection found"
   → UI shows warning

2. Inspection rejected
   → Return "HOLD|Inspection rejected"
   → UI shows warning

3. Rating too low
   → Return "HOLD|Rating 2.1/5, need 3.0+"
   → UI shows warning

4. Database connection lost
   → ROLLBACK
   → Return "ERROR|Connection timeout"
   → UI shows error

5. Success
   → COMMIT
   → Return "RELEASED|Payment released..."
   → UI shows success + refreshes
```

---

## 11. State Machine: Milestone Status

### Milestone Progression

```
┌──────────┐
│ PENDING  │  (Initial state - created by admin)
└────┬─────┘
     │ Contractor submits
     ▼
┌──────────┐
│SUBMITTED │  (Waiting for inspection)
└────┬─────┘
     │ Inspector reviews work
     │
  ┌──┴──────────────┐
  │                 │
  ▼                 ▼
┌─────────┐   ┌──────────────┐
│APPROVED │   │   REJECTED   │  (Contractor must redo)
└────┬────┘   └──────────────┘
     │                 │
     │        Contractor resubmits
     │                 │
     │                 └──→ Back to SUBMITTED
     │
     ▼
┌──────────┐
│SUBMITTED │  (Ready for payment - if rules met)
└────┬─────┘
     │
  ┌──┴───────────────┐
  │                  │
YES             NO (rules failed)
  │                  │
  ▼                  ▼
PAID               HOLD
  │              (stays SUBMITTED)
  │              Admin must retry
  │
  └─→ Project progress
```

---

## 12. User Role Workflows Summary

### What Each Role Does

```
┌─────────────────────────────────────────────────────────┐
│                     ADMIN WORKFLOW                      │
├─────────────────────────────────────────────────────────┤
│ 1. Create projects                                      │
│ 2. Add milestones to projects                           │
│ 3. Assign contractors to projects                       │
│ 4. Assign inspectors to milestones                      │
│ 5. Review payment requests                              │
│ 6. Release payments (when rules met)                    │
│ 7. View all system data                                 │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│               CONTRACTOR WORKFLOW                       │
├─────────────────────────────────────────────────────────┤
│ 1. View assigned projects                               │
│ 2. Complete work on milestones                          │
│ 3. Submit milestones for inspection                     │
│ 4. Make corrections if needed                           │
│ 5. Track payment status                                 │
│ 6. View public feedback                                 │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│               INSPECTOR WORKFLOW                        │
├─────────────────────────────────────────────────────────┤
│ 1. View pending milestones                              │
│ 2. Inspect work quality                                 │
│ 3. Approve/Reject/Request rework                        │
│ 4. Add inspection remarks                               │
│ 5. Submit inspection                                    │
│ 6. Review inspection history                            │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                 PUBLIC USER WORKFLOW                    │
├─────────────────────────────────────────────────────────┤
│ 1. Browse active projects                               │
│ 2. View project details                                 │
│ 3. Submit 1-5 star rating                               │
│ 4. Leave optional comments                              │
│ 5. View other user feedback                             │
│ 6. Track average project rating                         │
└─────────────────────────────────────────────────────────┘
```

---

## Key Flows Summary Table

| Flow | Trigger | Key Steps | Outcome |
|------|---------|-----------|---------|
| **Authentication** | User enters credentials | Query DB → Match email/password | Route to dashboard or error |
| **Project Creation** | Admin creates project | Enter details → Save to DB → ID assigned | Project ACTIVE, ready for milestones |
| **Milestone Submission** | Contractor submits work | Status: PENDING→SUBMITTED | Ready for inspector review |
| **Inspection** | Inspector reviews | Approve/Reject work → Add remarks | Status changed, DB updated |
| **Payment Release** | Admin releases payment | Check inspection + rating → Update both tables → Commit TX | Payment RELEASED + Milestone PAID |
| **Public Feedback** | Public rates project | Submit rating + comment → Store in DB → Calculate avg | Average used in payment decisions |
| **Payment Denial** | Any rule fails | ROLLBACK TX → Keep HOLD → Show reason | No changes, payment stays HOLD |

---

## Critical Decision Points

```
Payment Release Checkpoint
        │
    ┌───┴────┐
    │        │
 ┌──▼──┐  ┌─▼──────┐
 │ Rule1│  │ Rule 2 │
 │ Insp │  │ Rating │
 │ App? │  │ >=3.0? │
 └──┬──┘  └─┬──────┘
    │      │
   ┌┴──────┘
   │
 ┌─┴─┐
 │AND│  Both must pass
 └─┬─┘
   │
┌──┴──┐
│    │
Y    N
│    │
✅  ❌
R   H
```

**All flows depend on this decision point being correct.**
