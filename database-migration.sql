-- ========================================
-- DATABASE MIGRATION SCRIPT
-- Change User ID from BIGINT to VARCHAR(20)
-- Custom ID Format: PMUMS2024XXXXX
-- ========================================

-- ⚠️ IMPORTANT: Backup your database before running this script!

-- Step 1: Create id_sequence table to track custom ID generation
CREATE TABLE IF NOT EXISTS id_sequence (
    sequence_name VARCHAR(50) PRIMARY KEY,
    current_value BIGINT NOT NULL
);

-- Step 2: Initialize the sequence starting from 58107 (so next ID will be 58108)
INSERT INTO id_sequence (sequence_name, current_value)
VALUES ('USER_ID', 58107)
ON DUPLICATE KEY UPDATE current_value = current_value;
-- For PostgreSQL use: ON CONFLICT (sequence_name) DO NOTHING;

-- ========================================
-- Step 3: DROP FOREIGN KEY CONSTRAINTS FIRST
-- ========================================
-- This is CRITICAL - must drop FK before changing column types

-- Find the foreign key constraint name (MySQL/H2)
-- Common constraint name: FK9llbb4mj9qe5l0krj2ro35u51 or similar

-- For MySQL/H2:
ALTER TABLE receipt DROP FOREIGN KEY FK9llbb4mj9qe5l0krj2ro35u51;

-- For PostgreSQL:
-- ALTER TABLE receipt DROP CONSTRAINT FK9llbb4mj9qe5l0krj2ro35u51;

-- If you don't know the constraint name, find it:
-- MySQL: SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
--        WHERE TABLE_NAME = 'receipt' AND COLUMN_NAME = 'user_id';
-- PostgreSQL: SELECT conname FROM pg_constraint WHERE conrelid = 'receipt'::regclass;

-- ========================================
-- Step 4: Handle existing users table
-- ========================================
-- OPTION A: If you want to keep existing data (RECOMMENDED if you have users)

-- Backup existing users
CREATE TABLE users_backup AS SELECT * FROM users;

-- Backup existing receipts
CREATE TABLE receipt_backup AS SELECT * FROM receipt;

-- Add a temporary column for new user IDs
ALTER TABLE users ADD COLUMN new_id VARCHAR(20);

-- Generate new IDs for existing users
-- For MySQL/H2:
UPDATE users
SET new_id = CONCAT('PMUMS2024', LPAD(id, 5, '0'))
WHERE new_id IS NULL;

-- For PostgreSQL:
-- UPDATE users
-- SET new_id = CONCAT('PMUMS2024', LPAD(CAST(id AS VARCHAR), 5, '0'))
-- WHERE new_id IS NULL;

-- Add temporary column for new user_id in receipt table
ALTER TABLE receipt ADD COLUMN new_user_id VARCHAR(20);

-- Update receipt foreign keys to new format
-- For MySQL/H2:
UPDATE receipt r
JOIN users u ON r.user_id = u.id
SET r.new_user_id = u.new_id;

-- For PostgreSQL:
-- UPDATE receipt r
-- SET new_user_id = u.new_id
-- FROM users u
-- WHERE r.user_id = u.id;

-- ========================================
-- Step 5: Drop old columns and rename new ones
-- ========================================

-- Drop old id column from users
ALTER TABLE users DROP PRIMARY KEY;
ALTER TABLE users DROP COLUMN id;

-- Rename new_id to id
ALTER TABLE users CHANGE COLUMN new_id id VARCHAR(20) NOT NULL;
-- For PostgreSQL: ALTER TABLE users RENAME COLUMN new_id TO id;

-- Add primary key back
ALTER TABLE users ADD PRIMARY KEY (id);

-- Drop old user_id from receipt
ALTER TABLE receipt DROP COLUMN user_id;

-- Rename new_user_id to user_id
ALTER TABLE receipt CHANGE COLUMN new_user_id user_id VARCHAR(20) NOT NULL;
-- For PostgreSQL: ALTER TABLE receipt RENAME COLUMN new_user_id TO user_id;

-- ========================================
-- Step 6: RECREATE FOREIGN KEY CONSTRAINT
-- ========================================

ALTER TABLE receipt
ADD CONSTRAINT FK_receipt_user
FOREIGN KEY (user_id)
REFERENCES users(id);

-- ========================================
-- Step 7: Update the sequence to current max value
-- ========================================
-- This ensures next user gets correct ID

UPDATE id_sequence
SET current_value = (
    SELECT COALESCE(MAX(CAST(SUBSTRING(id, 10) AS UNSIGNED)), 58107)
    FROM users
)
WHERE sequence_name = 'USER_ID';

-- For PostgreSQL:
-- UPDATE id_sequence
-- SET current_value = (
--     SELECT COALESCE(MAX(CAST(SUBSTRING(id FROM 10) AS BIGINT)), 58107)
--     FROM users
-- )
-- WHERE sequence_name = 'USER_ID';

-- ========================================
-- OPTION B: If you want to start fresh (WARNING: Deletes all user data!)
-- ========================================
-- Uncomment below if you want to start with empty users table

-- DROP TABLE IF EXISTS users CASCADE;
-- CREATE TABLE users (
--     id VARCHAR(20) PRIMARY KEY,
--     name VARCHAR(255),
--     surname VARCHAR(255),
--     country_code VARCHAR(10),
--     phone_number VARCHAR(20),
--     email VARCHAR(255),
--     gender VARCHAR(20),
--     marital_status VARCHAR(50),
--     username VARCHAR(255) UNIQUE NOT NULL,
--     mobile_number VARCHAR(20),
--     password_hash VARCHAR(255),
--     home_address TEXT,
--     date_of_birth DATE,
--     school_office_name VARCHAR(255),
--     department VARCHAR(255),
--     department_unique_id VARCHAR(255),
--     department_district VARCHAR(255),
--     department_block VARCHAR(255),
--     nominee1_name VARCHAR(255),
--     nominee1_relation VARCHAR(100),
--     nominee2_name VARCHAR(255),
--     nominee2_relation VARCHAR(100),
--     accepted_terms BOOLEAN DEFAULT FALSE,
--     role VARCHAR(50) DEFAULT 'ROLE_USER',
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );

-- ========================================
-- VERIFICATION QUERIES
-- ========================================

-- Check sequence value
SELECT * FROM id_sequence WHERE sequence_name = 'USER_ID';

-- Check users table structure
-- PostgreSQL:
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'users' AND column_name = 'id';

-- Check existing users (if any)
SELECT id, username, name FROM users LIMIT 10;

-- ========================================
-- ROLLBACK (if needed)
-- ========================================
-- If migration fails, restore from backup:
-- DROP TABLE users;
-- ALTER TABLE users_backup RENAME TO users;

