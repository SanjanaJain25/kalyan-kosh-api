-- Migration script to update monthly_sahyog table structure
-- This script updates the monthly_sahyog table to use sahyog_date instead of month/year

-- Add new sahyog_date column
ALTER TABLE monthly_sahyog ADD COLUMN IF NOT EXISTS sahyog_date DATE;

-- Migrate existing data (convert month/year to date - first day of month)
UPDATE monthly_sahyog SET sahyog_date = STR_TO_DATE(CONCAT(year, '-', month, '-01'), '%Y-%m-%d')
WHERE sahyog_date IS NULL AND month IS NOT NULL AND year IS NOT NULL;

-- Drop old columns (uncomment after verifying data migration)
-- ALTER TABLE monthly_sahyog DROP COLUMN month;
-- ALTER TABLE monthly_sahyog DROP COLUMN year;

