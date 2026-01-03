-- =====================================================
-- User Entity Migration Script
-- Date: January 2, 2026
-- Purpose: Add new fields and update User table structure
-- =====================================================

-- Add new columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS father_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS joining_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS retirement_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS sankul_name VARCHAR(255);

-- Update email column to ensure it's unique and not null
-- Note: This will fail if there are NULL or duplicate emails
-- Make sure to clean data before running this
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);

-- Optional: Remove username column if no longer needed
-- WARNING: Only run this after ensuring all authentication is using email
-- and after backing up the data
-- ALTER TABLE users DROP COLUMN username;

-- Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_joining_date ON users(joining_date);
CREATE INDEX IF NOT EXISTS idx_users_retirement_date ON users(retirement_date);

-- Update existing records - set default values for new fields if needed
-- UPDATE users SET father_name = 'Not Specified' WHERE father_name IS NULL;
-- UPDATE users SET joining_date = created_at WHERE joining_date IS NULL;

-- Verify the changes
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_KEY
FROM
    INFORMATION_SCHEMA.COLUMNS
WHERE
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'users'
ORDER BY
    ORDINAL_POSITION;

-- =====================================================
-- End of Migration Script
-- =====================================================

