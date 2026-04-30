# Nirikshan - Construction Project Payment Management System

## Overview

**Nirikshan** is a desktop Java application designed to manage construction projects, track payments, and handle quality inspections. The system streamlines the workflow between multiple stakeholders including administrators, contractors, inspectors, and the public.

## Project Purpose

This system solves key challenges in construction project management:
- **Payment Authorization**: Ensures payments are released only when quality standards are met
- **Milestone Tracking**: Breaks projects into manageable milestones with budget allocation
- **Quality Control**: Mandatory inspections before payment release
- **Public Feedback**: Collects and monitors public ratings for accountability
- **Role-Based Access**: Different dashboards for different user types

## Key Features

### 1. Multi-Role System
- **Admin**: Create projects, assign contractors & inspectors, manage payments
- **Contractor**: Submit milestones, track project progress
- **Inspector**: Inspect milestones, approve/reject work
- **Public**: View projects and submit feedback/ratings

### 2. Milestone-Based Payments
- Projects divided into milestones
- Each milestone requires approval by inspection
- Payment released only if conditions met

### 3. Smart Payment Release Logic
- ✅ Latest inspection must be APPROVED
- ✅ Average public rating must be ≥ 3.0/5
- If both pass → Payment RELEASED
- If either fails → Payment stays HOLD

### 4. Quality Assurance
- Inspections required for every milestone
- Inspectors can approve, reject, or request rework
- Remarks recorded for transparency

### 5. Public Accountability
- 1-5 star rating system
- Comment-based feedback
- Average ratings affect payment decisions

## Technology Stack

- **Language**: Java 8+
- **GUI**: Swing (Desktop application)
- **Database**: PostgreSQL
- **Architecture**: DAO (Data Access Object) Pattern with Service Layer

## Project Structure

```
Nirikshan/
├── src/
│   ├── model/         # Data models (User, Project, Milestone, etc.)
│   ├── dao/           # Data Access Objects (repository layer)
│   ├── service/       # Business logic services
│   ├── ui/            # GUI components (Swing)
│   └── util/          # Utilities (DB connection, etc.)
├── db/
│   └── schema.sql     # PostgreSQL database schema
├── bin/               # Compiled classes
├── lib/               # External libraries
└── Nirikshan.iml      # IntelliJ project file
```

## Quick Start

1. **Setup Database**: Run `db/schema.sql` on PostgreSQL
2. **Configure Connection**: Update credentials in `DBConnection.java`
3. **Compile**: `javac -d bin src/**/*.java`
4. **Run**: `java -cp bin LoginScreen`

## Key Workflows

### Payment Release Workflow
```
Contractor submits milestone → Inspector inspects → 
Public rates project → System checks conditions → 
Payment released or held
```

### User Authentication Flow
```
Login Screen → Validate credentials → Route to appropriate dashboard
(Admin/Contractor/Inspector/Public)
```

## Database Entities

- **users**: All system users with roles
- **projects**: Construction projects assigned to contractors
- **milestones**: Project breakdown with budgets
- **inspections**: Quality checks on milestones
- **payments**: Payment records linked to milestones
- **public_feedback**: User ratings and comments

## Default Test Users

| Email | Password | Role |
|-------|----------|------|
| admin@pwqpvs.gov | admin123 | ADMIN |
| suresh@builds.com | pass123 | CONTRACTOR |
| meera@inspect.gov | inspect123 | INSPECTOR |
| arjun@citizen.com | pub123 | PUBLIC |

## Documentation Files

See the following files for detailed documentation:

- [ARCHITECTURE.md](ARCHITECTURE.md) - System design and architecture
- [DATABASE.md](DATABASE.md) - Database schema details
- [MODELS.md](MODELS.md) - Data model documentation
- [DAO.md](DAO.md) - Data access layer explanation
- [SERVICES.md](SERVICES.md) - Business logic services
- [UI.md](UI.md) - User interface components
- [UTILITIES.md](UTILITIES.md) - Utility classes
- [SETUP.md](SETUP.md) - Installation and setup guide

## Core Concepts

### DAO Pattern
The system uses the DAO (Data Access Object) pattern to separate data access logic from business logic. Each entity has a corresponding DAO class handling database operations.

### Service Layer
Business logic is implemented in service classes. Currently, `PaymentService` handles complex payment release logic with database transactions.

### Role-Based Dashboard
Each user role has a specialized dashboard:
- Admin can manage all projects and users
- Contractors manage their projects
- Inspectors conduct milestone inspections
- Public users view projects and leave feedback

## Important Notes

⚠️ **Security**: Current implementation uses plain-text passwords. In production, use proper password hashing and encryption.

⚠️ **Database**: Connection credentials are hardcoded in `DBConnection.java`. Use environment variables or config files in production.

⚠️ **Transaction Safety**: `PaymentService` uses database transactions to ensure data consistency during complex operations.

## Contact

For questions or issues, refer to the documentation files or contact the development team.
