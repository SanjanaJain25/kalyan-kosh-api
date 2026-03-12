-- ===============================================================
-- Database Migration Script for PMUMS User Entity Changes
-- ===============================================================
-- This script updates the users table to match the new User entity structure

USE your_database_name; -- Replace with your actual database name

-- ===============================================================
-- 1. ADD NEW COLUMNS
-- ===============================================================

-- Add father name column
ALTER TABLE users
ADD COLUMN father_name VARCHAR(255) AFTER surname;

-- Add joining date and retirement date columns
ALTER TABLE users
ADD COLUMN joining_date DATE AFTER date_of_birth;

ALTER TABLE users
ADD COLUMN retirement_date DATE AFTER joining_date;

-- ===============================================================
-- 2. RENAME COLUMNS
-- ===============================================================

-- Rename school_office_name to sankul_name
ALTER TABLE users
CHANGE COLUMN school_office_name sankul_name VARCHAR(255);

-- ===============================================================
-- 3. DROP USERNAME COLUMN AND CONSTRAINTS
-- ===============================================================

-- Drop unique constraint on username if it exists
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE table_schema = DATABASE()
     AND table_name = 'users'
     AND index_name = 'UK_username') > 0,
    'ALTER TABLE users DROP INDEX UK_username',
    'SELECT "No username unique constraint to drop"'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop the username column
ALTER TABLE users DROP COLUMN username;

-- ===============================================================
-- 4. CREATE NEW INDEXES FOR PERFORMANCE
-- ===============================================================

-- Create index on email for faster authentication lookups
CREATE INDEX idx_users_email ON users(email);

-- Create index on mobile number for faster lookups
CREATE INDEX idx_users_mobile ON users(mobile_number);

-- Create index on father name for search functionality
CREATE INDEX idx_users_father_name ON users(father_name);

-- Create index on sankul name
CREATE INDEX idx_users_sankul_name ON users(sankul_name);

-- ===============================================================
-- 5. UPDATE EXISTING DATA (OPTIONAL)
-- ===============================================================

-- Set default values for new columns if needed
-- UPDATE users SET father_name = 'Not Specified' WHERE father_name IS NULL;
-- UPDATE users SET joining_date = '2020-01-01' WHERE joining_date IS NULL;

-- ===============================================================
-- 6. VERIFY MIGRATION
-- ===============================================================

-- Check the updated table structure
DESCRIBE users;

-- Count records to ensure no data loss
SELECT COUNT(*) as total_users FROM users;

-- Display success message
SELECT '✅ Database migration completed successfully!' as status,
       'Changes: Removed username, Added father_name, joining_date, retirement_date, Renamed school_office_name to sankul_name' as changes;
