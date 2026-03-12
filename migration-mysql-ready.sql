-- ========================================
-- MYSQL MIGRATION SCRIPT - READY TO RUN
-- Change User ID from BIGINT to VARCHAR(20)
-- Format: PMUMS2024XXXXX
-- ========================================

-- ⚠️ BACKUP YOUR DATABASE FIRST!
-- mysqldump -u root -p kalyankosh_db > backup_$(date +%Y%m%d).sql

USE kalyankosh_db;

-- ========================================
-- STEP 1: Create id_sequence table
-- ========================================
CREATE TABLE IF NOT EXISTS id_sequence (
    sequence_name VARCHAR(50) PRIMARY KEY,
    current_value BIGINT NOT NULL
);

INSERT INTO id_sequence (sequence_name, current_value)
VALUES ('USER_ID', 58107)
ON DUPLICATE KEY UPDATE current_value = current_value;

-- ========================================
-- STEP 2: Find and drop foreign key constraint
-- ========================================

-- First, let's see what foreign keys exist
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'receipt'
  AND COLUMN_NAME = 'user_id'
  AND CONSTRAINT_SCHEMA = 'kalyankosh_db';

-- Drop the foreign key (adjust name if different)
-- From your error: FK9llbb4mj9qe5l0krj2ro35u51
ALTER TABLE receipt DROP FOREIGN KEY FK9llbb4mj9qe5l0krj2ro35u51;

-- ========================================
-- STEP 3: Backup existing data
-- ========================================
CREATE TABLE IF NOT EXISTS users_backup AS SELECT * FROM users;
CREATE TABLE IF NOT EXISTS receipt_backup AS SELECT * FROM receipt;

-- ========================================
-- STEP 4: Add temporary columns
-- ========================================
ALTER TABLE users ADD COLUMN new_id VARCHAR(20) DEFAULT NULL;
ALTER TABLE receipt ADD COLUMN new_user_id VARCHAR(20) DEFAULT NULL;

-- ========================================
-- STEP 5: Generate new IDs for existing users
-- ========================================
UPDATE users
SET new_id = CONCAT('PMUMS2024', LPAD(id, 5, '0'))
WHERE new_id IS NULL;

-- Verify the conversion
SELECT id AS old_id, new_id, username FROM users LIMIT 10;

-- ========================================
-- STEP 6: Update receipt foreign keys
-- ========================================
UPDATE receipt r
INNER JOIN users u ON r.user_id = u.id
SET r.new_user_id = u.new_id;

-- Verify the conversion
SELECT
    r.id AS receipt_id,
    r.user_id AS old_user_id,
    r.new_user_id,
    u.username
FROM receipt r
INNER JOIN users u ON r.user_id = u.id
LIMIT 10;

-- Check if any receipts were not updated
SELECT COUNT(*) AS receipts_not_updated
FROM receipt
WHERE new_user_id IS NULL;
-- Should be 0

-- ========================================
-- STEP 7: Drop old columns and constraints
-- ========================================

-- Drop primary key from users
ALTER TABLE users DROP PRIMARY KEY;

-- Drop old id column
ALTER TABLE users DROP COLUMN id;

-- Drop old user_id from receipt
ALTER TABLE receipt DROP COLUMN user_id;

-- ========================================
-- STEP 8: Rename new columns to original names
-- ========================================
ALTER TABLE users CHANGE COLUMN new_id id VARCHAR(20) NOT NULL;
ALTER TABLE receipt CHANGE COLUMN new_user_id user_id VARCHAR(20) NOT NULL;

-- ========================================
-- STEP 9: Add primary key back to users
-- ========================================
ALTER TABLE users ADD PRIMARY KEY (id);

-- ========================================
-- STEP 10: Recreate foreign key constraint
-- ========================================
ALTER TABLE receipt
ADD CONSTRAINT FK_receipt_user
FOREIGN KEY (user_id)
REFERENCES users(id)
ON DELETE RESTRICT
ON UPDATE CASCADE;

-- ========================================
-- STEP 11: Update sequence to max existing ID
-- ========================================
UPDATE id_sequence
SET current_value = (
    SELECT COALESCE(
        MAX(CAST(SUBSTRING(id, 10) AS UNSIGNED)),
        58107
    )
    FROM users
)
WHERE sequence_name = 'USER_ID';

-- ========================================
-- VERIFICATION
-- ========================================

-- 1. Check table structures
DESCRIBE users;
DESCRIBE receipt;

-- 2. Check primary key
SHOW KEYS FROM users WHERE Key_name = 'PRIMARY';

-- 3. Check foreign key
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'receipt'
  AND COLUMN_NAME = 'user_id'
  AND CONSTRAINT_SCHEMA = 'kalyankosh_db';

-- 4. Check sample data
SELECT u.id, u.username, u.name
FROM users u
LIMIT 10;

-- 5. Check receipt-user relationship
SELECT
    r.id AS receipt_id,
    r.user_id,
    u.username,
    u.name
FROM receipt r
INNER JOIN users u ON r.user_id = u.id
LIMIT 10;

-- 6. Check sequence
SELECT * FROM id_sequence WHERE sequence_name = 'USER_ID';

-- 7. Count records
SELECT
    (SELECT COUNT(*) FROM users) AS total_users,
    (SELECT COUNT(*) FROM receipt) AS total_receipts,
    (SELECT current_value FROM id_sequence WHERE sequence_name = 'USER_ID') AS next_id;

-- ========================================
-- SUCCESS MESSAGE
-- ========================================
SELECT 'Migration completed successfully!' AS status;

-- ========================================
-- ROLLBACK (if needed)
-- ========================================
-- IF SOMETHING GOES WRONG, RUN THIS:
/*
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS receipt;
RENAME TABLE users_backup TO users;
RENAME TABLE receipt_backup TO receipt;
DROP TABLE IF EXISTS id_sequence;
*/

