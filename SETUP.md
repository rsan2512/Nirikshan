# Setup and Installation Guide

## Prerequisites

### System Requirements

| Component | Requirement | Version |
|-----------|-------------|---------|
| Java | JDK installed | Java 8 or higher |
| PostgreSQL | Database server | 12 or higher |
| IDE (Optional) | IntelliJ IDEA or Eclipse | Latest recommended |
| OS | Windows/Linux/macOS | Any with Java support |

### Download Required Software

1. **Java Development Kit (JDK)**
   - Download: https://www.oracle.com/java/technologies/downloads/
   - Choose JDK 8 or higher
   - Install and verify: `java -version`

2. **PostgreSQL**
   - Download: https://www.postgresql.org/download/
   - Install server and client tools
   - Note the password set during installation

3. **PostgreSQL JDBC Driver** (if not included)
   - Download: https://jdbc.postgresql.org/
   - Add JAR to project classpath

---

## Installation Steps

### Step 1: Create PostgreSQL Database

**Open Terminal/Command Prompt**:

#### Windows (Command Prompt)
```cmd
# Connect to PostgreSQL
psql -U postgres -h localhost

# You'll be prompted for password (set during PostgreSQL installation)
```

#### Linux/macOS (Terminal)
```bash
sudo -u postgres psql
```

**In PostgreSQL Shell**:
```sql
-- Create database
CREATE DATABASE pwqpvs_db;

-- Connect to database
\c pwqpvs_db

-- Run schema file
\i path/to/db/schema.sql

-- Verify tables created
\dt  -- List all tables

-- Verify sample data
SELECT * FROM users;  -- Should show 4 test users
```

**Verify Connection**:
```sql
-- Check user count
SELECT COUNT(*) FROM users;

-- Should output: 4

-- List all tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public';
```

---

### Step 2: Configure Database Connection

**File**: `src/util/DBConnection.java`

Update credentials:
```java
private static final String URL = "jdbc:postgresql://localhost:5432/pwqpvs_db";
private static final String USER = "postgres";
private static final String PASSWORD = "YOUR_PASSWORD_HERE";  // Change this
```

**Important**: 
- Replace `YOUR_PASSWORD_HERE` with the PostgreSQL password set during installation
- Ensure PostgreSQL is running on localhost:5432 (default port)

---

### Step 3: Compile the Project

**Option 1: Command Line**

Navigate to project directory:
```bash
cd d:\projects\Nirikshan
```

Compile all Java files:
```bash
javac -d bin src/model/*.java src/dao/*.java src/service/*.java src/ui/*.java src/util/*.java
```

Verify compilation:
```bash
# Should see .class files
dir bin\model\
dir bin\dao\
```

**Option 2: IDE (IntelliJ IDEA)**

1. Open project in IntelliJ
2. File → Project Structure → Project
3. Set Project SDK to Java 8+
4. Build → Build Project
5. Check "Build" panel for compilation status

**Option 3: IDE (Eclipse)**

1. File → Open Projects from File System
2. Select project directory
3. Project → Build Project
4. Check Problems view for errors

---

### Step 4: Run the Application

**From Command Line**:

```bash
# Navigate to project directory
cd d:\projects\Nirikshan

# Run with Java
java -cp bin ui.LoginScreen
```

**From IDE**:
1. Right-click LoginScreen.java
2. Select "Run LoginScreen.main()"

**Expected Output**:
```
Login Screen should appear
PostgreSQL connection successful
Ready for login
```

---

## Database Setup Detailed Steps

### Creating Database Schema

**Complete Schema Creation**:
```sql
-- Connect as postgres
psql -U postgres

-- Create database
CREATE DATABASE pwqpvs_db;

-- Connect to database
\c pwqpvs_db

-- Create tables (run entire schema.sql file)
-- OR manually execute table creation commands:

CREATE TABLE users (
    user_id     SERIAL PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(100)    UNIQUE NOT NULL,
    password    VARCHAR(100)    NOT NULL,
    role        VARCHAR(20)     NOT NULL 
                CHECK (role IN ('ADMIN','CONTRACTOR','INSPECTOR','PUBLIC')),
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- ... (repeat for other tables)

-- Insert test data
INSERT INTO users (name, email, password, role) VALUES
('Admin Ravi',        'admin@pwqpvs.gov',    'admin123',    'ADMIN'),
('Contractor Suresh', 'suresh@builds.com',   'pass123',     'CONTRACTOR'),
('Inspector Meera',   'meera@inspect.gov',   'inspect123',  'INSPECTOR'),
('Public Arjun',      'arjun@citizen.com',   'pub123',      'PUBLIC');
```

### Verify Database Setup

```sql
-- Check database created
\l  -- List all databases

-- Check tables created
\dt

-- Count records
SELECT COUNT(*) FROM users;         -- Should show 4
SELECT COUNT(*) FROM projects;      -- Should show 0
SELECT COUNT(*) FROM milestones;    -- Should show 0
```

### Troubleshooting Database Setup

| Issue | Solution |
|-------|----------|
| Cannot connect to PostgreSQL | Verify PostgreSQL is running: `pg_isready` |
| Password incorrect | Reset password: `ALTER USER postgres WITH PASSWORD 'newpassword';` |
| Database already exists | Drop and recreate: `DROP DATABASE pwqpvs_db;` |
| Permission denied | Ensure using postgres user or admin account |
| Schema file not found | Verify path to schema.sql is correct |
| Port 5432 in use | Change port or stop other PostgreSQL instance |

---

## Compilation Troubleshooting

### Common Compilation Errors

**Error**: `javac: command not found`
```
Solution: Java not in PATH
- Windows: Add C:\Program Files\Java\jdkX.X.X\bin to PATH
- Linux: Run: export PATH=/usr/lib/jvm/java-8-openjdk/bin:$PATH
```

**Error**: `package xxx does not exist`
```
Solution: Source files not compiled or missing imports
- Ensure all .java files are in src/ directory structure
- Check import statements reference correct packages
```

**Error**: `cannot find symbol`
```
Solution: Missing class or method
- Verify class exists in corresponding package
- Check spelling of class/method name
- Ensure public access modifier
```

**Error**: `classpath not set`
```
Solution: Missing JDBC driver
- Add postgresql-XX.jar to lib/ directory
- Include in compile: javac -cp lib/* -d bin src/**/*.java
```

---

## Runtime Troubleshooting

### Common Runtime Errors

**Error**: `SQLException: Connection refused`
```
Solution: PostgreSQL not running or wrong host
- Windows: Start PostgreSQL from Services
- Linux: sudo systemctl start postgresql
- Verify host/port in DBConnection.java
```

**Error**: `SQLException: password authentication failed`
```
Solution: Wrong database password
- Update password in DBConnection.java
- Verify password in PostgreSQL
```

**Error**: `Exception in thread "main" java.lang.ClassNotFoundException`
```
Solution: Class not found or compilation issue
- Recompile: javac -d bin src/**/*.java
- Ensure class files generated in bin/
- Run with correct classpath: java -cp bin LoginScreen
```

**Error**: `NoClassDefFoundError: org/postgresql/Driver`
```
Solution: PostgreSQL JDBC driver not in classpath
- Download: https://jdbc.postgresql.org/
- Add JAR to lib/ directory
- Update classpath: java -cp bin:lib/* LoginScreen
```

**Error**: NullPointerException at login
```
Solution: Database tables not created or empty
- Verify schema.sql was executed
- Check tables exist: psql -U postgres pwqpvs_db -c "\dt"
- Check test data inserted: psql -U postgres pwqpvs_db -c "SELECT * FROM users;"
```

---

## Development Environment Setup

### IDE Configuration

#### IntelliJ IDEA
1. **Open Project**
   - File → Open → Select Nirikshan folder

2. **Configure JDK**
   - File → Project Structure → Project
   - Set Project SDK to Java 8+

3. **Add PostgreSQL Driver**
   - File → Project Structure → Libraries
   - Click + → Java
   - Navigate to postgresql-XX.jar
   - Add to project

4. **Create Run Configuration**
   - Run → Edit Configurations
   - Click + → Application
   - Main class: `ui.LoginScreen`
   - Working directory: `$PROJECT_DIR$`
   - Click OK

5. **Run Application**
   - Run → Run 'LoginScreen'

#### Eclipse
1. **Create Project**
   - File → New → Java Project
   - Project name: Nirikshan
   - Link to external source folders

2. **Add Source Folders**
   - Build Path → Link Source
   - Add src, dao, model, service, ui, util folders

3. **Add PostgreSQL Driver**
   - Right-click project → Build Path → Configure Build Path
   - Libraries → Add External JAR
   - Select postgresql-XX.jar

4. **Create Run Configuration**
   - Run → Run Configurations
   - New Java Application
   - Main class: `ui.LoginScreen`

5. **Run Application**
   - Click Run

---

## Project Structure After Setup

```
Nirikshan/
├── src/
│   ├── dao/
│   │   ├── UserDAO.java
│   │   ├── ProjectDAO.java
│   │   ├── MilestoneDAO.java
│   │   ├── InspectionDAO.java
│   │   ├── PaymentDAO.java
│   │   └── FeedbackDAO.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Project.java
│   │   ├── Milestone.java
│   │   ├── Inspection.java
│   │   ├── Payment.java
│   │   └── PublicFeedback.java
│   ├── service/
│   │   ├── PaymentService.java
│   │   └── ProjectService.java
│   ├── ui/
│   │   ├── LoginScreen.java
│   │   ├── AdminDashboard.java
│   │   ├── ContractorDashboard.java
│   │   ├── InspectorDashboard.java
│   │   ├── PublicViewScreen.java
│   │   ├── NavBar.java
│   │   ├── JButtonStyled.java
│   │   ├── RoundedPanel.java
│   │   └── Theme.java
│   └── util/
│       └── DBConnection.java
├── bin/  ← Compiled .class files (created after javac)
│   ├── dao/
│   ├── model/
│   ├── service/
│   ├── ui/
│   └── util/
├── db/
│   └── schema.sql
├── lib/  ← External JARs (add postgresql-XX.jar here)
│   └── postgresql-42.6.0.jar
├── Nirikshan.iml
└── Documentation (markdown files you created)
    ├── README.md
    ├── ARCHITECTURE.md
    ├── DATABASE.md
    ├── MODELS.md
    ├── DAO.md
    ├── SERVICES.md
    ├── UI.md
    ├── UTILITIES.md
    └── SETUP.md
```

---

## First Run Checklist

- [ ] Java installed and `java -version` works
- [ ] PostgreSQL installed and running
- [ ] Database `pwqpvs_db` created
- [ ] Schema tables created
- [ ] Test data inserted (4 users)
- [ ] DBConnection.java password updated
- [ ] Project compiled without errors
- [ ] LoginScreen runs and displays
- [ ] Can login with test credentials
- [ ] Dashboard displays after login

---

## Useful PostgreSQL Commands

```sql
-- Connect to PostgreSQL
psql -U postgres -h localhost

-- List all databases
\l

-- Connect to specific database
\c pwqpvs_db

-- List all tables
\dt

-- Describe table structure
\d users

-- Show table data
SELECT * FROM users;

-- Count records
SELECT COUNT(*) FROM users;

-- Reset database
DROP DATABASE pwqpvs_db;
CREATE DATABASE pwqpvs_db;
\c pwqpvs_db
\i db/schema.sql

-- Exit PostgreSQL
\q
```

---

## Performance Tips

1. **Add Database Indexes**
   ```sql
   CREATE INDEX idx_projects_contractor_id ON projects(contractor_id);
   CREATE INDEX idx_milestones_project_id ON milestones(project_id);
   ```

2. **Use Connection Pooling** (for production)
   - Add HikariCP: `com.zaxxer:HikariCP:5.0.0`
   - Update DBConnection to use pooling

3. **Implement Caching**
   - Cache contractor/inspector lists
   - Refresh on changes

4. **Monitor Database**
   ```sql
   -- Check slow queries
   SELECT * FROM pg_stat_statements;
   ```

---

## Security Considerations

⚠️ **Current Implementation Issues**:
1. Hardcoded credentials in source code
2. Plain-text passwords in database
3. No input validation on UI
4. No encryption for data in transit

**Production Recommendations**:
1. Move credentials to environment variables
2. Implement password hashing (bcrypt)
3. Add input validation and sanitization
4. Use HTTPS for remote database
5. Implement role-based access control (RBAC)
6. Add audit logging
7. Regular security updates

---

## Next Steps

1. Run application and test all dashboards
2. Create sample projects and milestones
3. Test payment release workflow
4. Review documentation files
5. Plan production deployment strategy
