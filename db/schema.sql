-- Connect as postgres user first
-- then run this

CREATE DATABASE pwqpvs_db;

\c pwqpvs_db

-- 1. Users
CREATE TABLE users (
    user_id     SERIAL PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(100)    UNIQUE NOT NULL,
    password    VARCHAR(100)    NOT NULL,
    role        VARCHAR(20)     NOT NULL 
                CHECK (role IN ('ADMIN','CONTRACTOR','INSPECTOR','PUBLIC')),
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 2. Projects
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

-- 3. Milestones
CREATE TABLE milestones (
    milestone_id    SERIAL PRIMARY KEY,
    project_id      INT             NOT NULL REFERENCES projects(project_id),
    description     VARCHAR(500)    NOT NULL,
    amount          NUMERIC(15,2)   NOT NULL,
    status          VARCHAR(20)     DEFAULT 'PENDING'
                    CHECK (status IN 
                    ('PENDING','SUBMITTED','APPROVED','REJECTED','PAID'))
);

-- 4. Inspections
CREATE TABLE inspections (
    inspection_id   SERIAL PRIMARY KEY,
    milestone_id    INT             NOT NULL REFERENCES milestones(milestone_id),
    inspector_id    INT             NOT NULL REFERENCES users(user_id),
    result          VARCHAR(20)     NOT NULL
                    CHECK (result IN ('APPROVED','REJECTED','NEEDS_REWORK')),
    remarks         TEXT,
    inspected_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 5. Payments
CREATE TABLE payments (
    payment_id      SERIAL PRIMARY KEY,
    milestone_id    INT             NOT NULL REFERENCES milestones(milestone_id),
    amount          NUMERIC(15,2)   NOT NULL,
    status          VARCHAR(20)     DEFAULT 'HOLD'
                    CHECK (status IN ('HOLD','RELEASED','REJECTED')),
    released_at     TIMESTAMP
);

-- 6. Public Feedback
CREATE TABLE public_feedback (
    feedback_id     SERIAL PRIMARY KEY,
    project_id      INT             NOT NULL REFERENCES projects(project_id),
    rating          INT             NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    submitted_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- Test users
INSERT INTO users (name, email, password, role) VALUES
('Admin Ravi',        'admin@pwqpvs.gov',    'admin123',    'ADMIN'),
('Contractor Suresh', 'suresh@builds.com',   'pass123',     'CONTRACTOR'),
('Inspector Meera',   'meera@inspect.gov',   'inspect123',  'INSPECTOR'),
('Public Arjun',      'arjun@citizen.com',   'pub123',      'PUBLIC');