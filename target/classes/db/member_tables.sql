-- ============================================================================
-- B'Groceries — Member Management tables (PostgreSQL)
-- ============================================================================
-- MANUAL / OPTIONAL migration. The Spring Boot app creates these tables itself
-- via Hibernate (ddl-auto: update) on both the dev (H2) and prod (Neon)
-- profiles; this script exists so you can own the DDL for Postgres if you
-- prefer. It mirrors the Hibernate-generated schema (snake_case physical names).
-- There is NO Flyway/Liquibase wiring — apply by hand, e.g.:
--   psql "$DB_URL" -f src/main/resources/db/member_tables.sql
-- ============================================================================

CREATE TABLE IF NOT EXISTS member (
    id           BIGSERIAL PRIMARY KEY,
    member_code  VARCHAR(50)  NOT NULL UNIQUE,
    full_name    VARCHAR(100) NOT NULL,
    position     VARCHAR(100),
    rank         INTEGER,
    department   VARCHAR(100),
    category     VARCHAR(100),
    photo_url    TEXT,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS member_detail (
    id                BIGSERIAL PRIMARY KEY,
    member_id         BIGINT NOT NULL UNIQUE REFERENCES member(id) ON DELETE CASCADE,
    phone_number      VARCHAR(20),
    email             VARCHAR(100),
    address           VARCHAR(255),
    date_of_birth     DATE,
    gender            VARCHAR(20),
    emergency_contact VARCHAR(100),
    start_date        DATE,
    note              TEXT,
    nationality       VARCHAR(100)
);

-- Indexes for the department/category list filters (list endpoint + admin UI selects).
CREATE INDEX IF NOT EXISTS idx_member_department ON member(department);
CREATE INDEX IF NOT EXISTS idx_member_category    ON member(category);

-- ============================================================================
-- Optional sample seeds (one-to-one member/member_detail pairs)
-- Remove this block if you do not want demo data.
-- ============================================================================
INSERT INTO member (member_code, full_name, position, rank, department, category, created_at, updated_at) VALUES
    ('M-001', 'Dara Sok',    'Store Manager', 1, 'Sales',     'Staff', NOW(), NOW()),
    ('M-002', 'Chanly Meas', 'Cashier',       2, 'Sales',     'Staff', NOW(), NOW()),
    ('M-003', 'Vannak Kim',  'Driver',        3, 'Logistics', 'Staff', NOW(), NOW())
ON CONFLICT (member_code) DO NOTHING;

INSERT INTO member_detail (member_id, phone_number, email, address, date_of_birth, gender, emergency_contact, start_date, note, nationality)
SELECT id, '+85512345678', 'dara@bgroceries.demo',   'Phnom Penh', '1995-05-10', 'Male',   '+85598765432', '2023-01-15', 'Full-time', 'Khmer'
FROM member WHERE member_code = 'M-001'
ON CONFLICT (member_id) DO NOTHING;

INSERT INTO member_detail (member_id, phone_number, email, address, date_of_birth, gender, emergency_contact, start_date, note, nationality)
SELECT id, '+85523456789', 'chanly@bgroceries.demo', 'Phnom Penh', '1998-11-02', 'Female', '+85587654321', '2023-06-01', 'Part-time', 'Khmer'
FROM member WHERE member_code = 'M-002'
ON CONFLICT (member_id) DO NOTHING;

INSERT INTO member_detail (member_id, phone_number, email, address, date_of_birth, gender, emergency_contact, start_date, note, nationality)
SELECT id, '+85534567890', 'vannak@bgroceries.demo', 'Kandal',     '1992-03-21', 'Male',   '+85576543210', '2022-09-01', 'Full-time', 'Khmer'
FROM member WHERE member_code = 'M-003'
ON CONFLICT (member_id) DO NOTHING;

-- ============================================================================
-- Real roster seeds (28 members) — Department/Category pairs supplied by the
-- user. full_name is a PLACEHOLDER ("Member N") and position/rank are NULL:
-- edit each row in the admin UI (Members -> Edit) to add real names, photos,
-- and positions. Re-run is safe (ON CONFLICT DO NOTHING).
-- ============================================================================
INSERT INTO member (member_code, full_name, position, rank, department, category, created_at, updated_at) VALUES
    ('MEM-001', 'Member 1',  NULL, NULL, 'Executive', 'Management',     NOW(), NOW()),
    ('MEM-002', 'Member 2',  NULL, NULL, 'Executive', 'Management',     NOW(), NOW()),
    ('MEM-003', 'Member 3',  NULL, NULL, 'Admin',     'Management',     NOW(), NOW()),
    ('MEM-004', 'Member 4',  NULL, NULL, 'Admin',     'Management',     NOW(), NOW()),
    ('MEM-005', 'Member 5',  NULL, NULL, 'Admin',     'Management',     NOW(), NOW()),
    ('MEM-006', 'Member 6',  NULL, NULL, 'Operation', 'Management',     NOW(), NOW()),
    ('MEM-007', 'Member 7',  NULL, NULL, 'Admin',     'Finance',        NOW(), NOW()),
    ('MEM-008', 'Member 8',  NULL, NULL, 'Admin',     'Supervisor',     NOW(), NOW()),
    ('MEM-009', 'Member 9',  NULL, NULL, 'Operation', 'Supervisor',     NOW(), NOW()),
    ('MEM-010', 'Member 10', NULL, NULL, 'Operation', 'Supervisor',     NOW(), NOW()),
    ('MEM-011', 'Member 11', NULL, NULL, 'Operation', 'Technology',     NOW(), NOW()),
    ('MEM-012', 'Member 12', NULL, NULL, 'Operation', 'Technology',     NOW(), NOW()),
    ('MEM-013', 'Member 13', NULL, NULL, 'Operation', 'Technology',     NOW(), NOW()),
    ('MEM-014', 'Member 14', NULL, NULL, 'Operation', 'Food & Service', NOW(), NOW()),
    ('MEM-015', 'Member 15', NULL, NULL, 'Operation', 'Food & Service', NOW(), NOW()),
    ('MEM-016', 'Member 16', NULL, NULL, 'Operation', 'Food & Service', NOW(), NOW()),
    ('MEM-017', 'Member 17', NULL, NULL, 'Operation', 'Food & Service', NOW(), NOW()),
    ('MEM-018', 'Member 18', NULL, NULL, 'Operation', 'Food & Service', NOW(), NOW()),
    ('MEM-019', 'Member 19', NULL, NULL, 'Operation', 'Food & Service', NOW(), NOW()),
    ('MEM-020', 'Member 20', NULL, NULL, 'Operation', 'Food & Service', NOW(), NOW()),
    ('MEM-021', 'Member 21', NULL, NULL, 'Operation', 'Security',       NOW(), NOW()),
    ('MEM-022', 'Member 22', NULL, NULL, 'Operation', 'Security',       NOW(), NOW()),
    ('MEM-023', 'Member 23', NULL, NULL, 'Operation', 'Cashier',        NOW(), NOW()),
    ('MEM-024', 'Member 24', NULL, NULL, 'Operation', 'Cashier',        NOW(), NOW()),
    ('MEM-025', 'Member 25', NULL, NULL, 'Operation', 'Cashier',        NOW(), NOW()),
    ('MEM-026', 'Member 26', NULL, NULL, 'Operation', 'Cashier',        NOW(), NOW()),
    ('MEM-027', 'Member 27', NULL, NULL, 'Operation', 'Cashier',        NOW(), NOW()),
    ('MEM-028', 'Member 28', NULL, NULL, 'Operation', 'Cashier',        NOW(), NOW())
ON CONFLICT (member_code) DO NOTHING;
