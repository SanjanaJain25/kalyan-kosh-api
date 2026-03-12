-- Migration to add new fields to users table
-- Date: 2026-01-02
-- Description: Add fatherName, joiningDate, retirementDate, sankulName fields

-- Add new columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS father_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS joining_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS retirement_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS sankul_name VARCHAR(255);

-- Note: school_office_name already exists, no need to add
-- The field represents: पदस्थ स्कूल/कार्यालय का नाम
-- The sankulName field represents: संकुल का नाम

-- Remove username column if it exists (now using email for authentication)
-- First, check if there's any data dependency
-- ALTER TABLE users DROP COLUMN IF EXISTS username;

COMMIT;

