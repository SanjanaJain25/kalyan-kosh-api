-- ================================================================================
-- DATABASE ROLE VERIFICATION SCRIPT
-- ================================================================================
-- Purpose: Verify that the 'role' column exists and has correct data
-- Run this in MySQL Workbench or command line
-- ================================================================================

USE kalyan_kosh;

-- 1. Check if 'role' column exists in 'users' table
-- ================================================================================
DESCRIBE users;

-- Expected output should include:
-- +--------+--------------+------+-----+---------+-------+
-- | Field  | Type         | Null | Key | Default | Extra |
-- +--------+--------------+------+-----+---------+-------+
-- | role   | varchar(255) | YES  |     | NULL    |       |
-- +--------+--------------+------+-----+---------+-------+


-- 2. Check existing roles in database
-- ================================================================================
SELECT DISTINCT role FROM users;

-- Expected output:
-- +-----------+
-- | role      |
-- +-----------+
-- | ROLE_USER |
-- +-----------+


-- 3. Check all users and their roles
-- ================================================================================
SELECT
    id,
    username,
    name,
    surname,
    role,
    created_at
FROM users
ORDER BY created_at DESC
LIMIT 10;

-- Expected output:
-- +----------------+------------------------+-------+---------+-----------+---------------------+
-- | id             | username               | name  | surname | role      | created_at          |
-- +----------------+------------------------+-------+---------+-----------+---------------------+
-- | PMUMS202458108 | ssaman7566@gmail.com   | Aman  | Soni    | ROLE_USER | 2025-12-30 08:52:15 |
-- +----------------+------------------------+-------+---------+-----------+---------------------+


-- 4. Check if any users have NULL role (needs fixing)
-- ================================================================================
SELECT
    id,
    username,
    name,
    role
FROM users
WHERE role IS NULL;

-- If this returns rows, run the fix below:
-- UPDATE users SET role = 'ROLE_USER' WHERE role IS NULL;


-- 5. Count users by role
-- ================================================================================
SELECT
    role,
    COUNT(*) as user_count
FROM users
GROUP BY role;

-- Expected output:
-- +-------------+------------+
-- | role        | user_count |
-- +-------------+------------+
-- | ROLE_USER   | 5          |
-- | ROLE_MANAGER| 2          |
-- | ROLE_ADMIN  | 1          |
-- +-------------+------------+


-- 6. Check specific test user
-- ================================================================================
SELECT
    id,
    username,
    name,
    role,
    created_at
FROM users
WHERE username = 'ssaman7566@gmail.com';

-- Expected output:
-- +----------------+----------------------+------+-----------+---------------------+
-- | id             | username             | name | role      | created_at          |
-- +----------------+----------------------+------+-----------+---------------------+
-- | PMUMS202458108 | ssaman7566@gmail.com | Aman | ROLE_USER | 2025-12-30 08:52:15 |
-- +----------------+----------------------+------+-----------+---------------------+


-- ================================================================================
-- OPTIONAL: Create test users with different roles
-- ================================================================================

-- Create MANAGER user (if doesn't exist)
-- INSERT INTO users (id, username, password_hash, name, role, created_at)
-- VALUES ('PMUMS202458109', 'manager@example.com', '$2a$10$...', 'Manager User', 'ROLE_MANAGER', NOW());

-- Create ADMIN user (if doesn't exist)
-- INSERT INTO users (id, username, password_hash, name, role, created_at)
-- VALUES ('PMUMS202458110', 'admin@example.com', '$2a$10$...', 'Admin User', 'ROLE_ADMIN', NOW());


-- ================================================================================
-- FIX: Update NULL roles to ROLE_USER
-- ================================================================================

-- Check count first
SELECT COUNT(*) as null_roles FROM users WHERE role IS NULL;

-- If count > 0, run this to fix:
-- UPDATE users SET role = 'ROLE_USER' WHERE role IS NULL;

-- Verify fix
-- SELECT COUNT(*) as null_roles FROM users WHERE role IS NULL;
-- Expected: 0


-- ================================================================================
-- FIX: Update specific user role
-- ================================================================================

-- Change user to MANAGER
-- UPDATE users SET role = 'ROLE_MANAGER' WHERE username = 'manager@example.com';

-- Change user to ADMIN
-- UPDATE users SET role = 'ROLE_ADMIN' WHERE username = 'admin@example.com';

-- Verify
-- SELECT username, role FROM users WHERE username IN ('manager@example.com', 'admin@example.com');


-- ================================================================================
-- VERIFICATION SUMMARY
-- ================================================================================

SELECT
    'Total Users' as metric,
    COUNT(*) as value
FROM users

UNION ALL

SELECT
    'Users with Role' as metric,
    COUNT(*) as value
FROM users
WHERE role IS NOT NULL

UNION ALL

SELECT
    'Users with NULL Role' as metric,
    COUNT(*) as value
FROM users
WHERE role IS NULL

UNION ALL

SELECT
    'ROLE_USER Count' as metric,
    COUNT(*) as value
FROM users
WHERE role = 'ROLE_USER'

UNION ALL

SELECT
    'ROLE_MANAGER Count' as metric,
    COUNT(*) as value
FROM users
WHERE role = 'ROLE_MANAGER'

UNION ALL

SELECT
    'ROLE_ADMIN Count' as metric,
    COUNT(*) as value
FROM users
WHERE role = 'ROLE_ADMIN';

-- Expected output:
-- +---------------------+-------+
-- | metric              | value |
-- +---------------------+-------+
-- | Total Users         | 10    |
-- | Users with Role     | 10    |
-- | Users with NULL Role| 0     |
-- | ROLE_USER Count     | 8     |
-- | ROLE_MANAGER Count  | 1     |
-- | ROLE_ADMIN Count    | 1     |
-- +---------------------+-------+


-- ================================================================================
-- END OF VERIFICATION SCRIPT
-- ================================================================================
-- ✅ If all queries return expected results, your database is configured correctly
-- ✅ Role column exists and has valid data
-- ✅ Ready to test login API and verify role in response
-- ================================================================================

