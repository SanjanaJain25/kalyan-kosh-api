-- PMUMS Database Quick Fix Script
-- Run this script to fix the 500 registration error
-- Database: kalyankosh_db

USE kalyankosh_db;

-- Show current table structure
SELECT 'BEFORE: Current table structure' as info;
DESCRIBE users;

-- Add missing columns safely (won't error if they already exist)
ALTER TABLE users ADD COLUMN IF NOT EXISTS father_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS joining_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS retirement_date DATE;

-- Check if school_office_name exists and rename it to sankul_name
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'kalyankosh_db'
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'school_office_name'
);

SET @sql = IF(@column_exists > 0,
    'ALTER TABLE users CHANGE COLUMN school_office_name sankul_name VARCHAR(255)',
    'SELECT "school_office_name column not found - already renamed or does not exist" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if username column still exists and drop it
SET @username_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'kalyankosh_db'
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'username'
);

SET @sql2 = IF(@username_exists > 0,
    'ALTER TABLE users DROP COLUMN username',
    'SELECT "username column already dropped or does not exist" as message'
);

PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Show updated table structure
SELECT 'AFTER: Updated table structure' as info;
DESCRIBE users;

-- Show success message
SELECT 'SUCCESS: Database schema updated for PMUMS registration!' as status;
SELECT 'Next step: Restart your Spring Boot application' as next_action;
