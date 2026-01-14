-- Migration script to update death_case table structure
-- This script updates the death_case table to support:
-- - Two nominees with QR codes for each
-- - Three account details as separate entity (bank name, account number, IFSC code, account holder name)
-- - Single caseDate field instead of caseMonth and caseYear
-- - Description field
-- - User image field

-- First, drop old columns if they exist
ALTER TABLE death_case DROP COLUMN IF EXISTS nominee_name;
ALTER TABLE death_case DROP COLUMN IF EXISTS nominee_account_number;
ALTER TABLE death_case DROP COLUMN IF EXISTS nominee_ifsc;
ALTER TABLE death_case DROP COLUMN IF EXISTS case_month;
ALTER TABLE death_case DROP COLUMN IF EXISTS case_year;

-- Create account_details table if not exists
CREATE TABLE IF NOT EXISTS account_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_name VARCHAR(255),
    account_number VARCHAR(255),
    ifsc_code VARCHAR(255),
    account_holder_name VARCHAR(255)
);

-- Add new fields
ALTER TABLE death_case ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE death_case ADD COLUMN IF NOT EXISTS user_image VARCHAR(255);
ALTER TABLE death_case ADD COLUMN IF NOT EXISTS case_date DATE;

-- Add Nominee 1 columns
ALTER TABLE death_case ADD COLUMN IF NOT EXISTS nominee1_name VARCHAR(255);
ALTER TABLE death_case ADD COLUMN IF NOT EXISTS nominee1_qr_code VARCHAR(255);

-- Add Nominee 2 columns
ALTER TABLE death_case ADD COLUMN IF NOT EXISTS nominee2_name VARCHAR(255);
ALTER TABLE death_case ADD COLUMN IF NOT EXISTS nominee2_qr_code VARCHAR(255);

-- Add foreign key columns for account details
ALTER TABLE death_case ADD COLUMN IF NOT EXISTS account1_id BIGINT;
ALTER TABLE death_case ADD COLUMN IF NOT EXISTS account2_id BIGINT;
ALTER TABLE death_case ADD COLUMN IF NOT EXISTS account3_id BIGINT;

-- Add foreign key constraints (optional - JPA handles this)
-- ALTER TABLE death_case ADD CONSTRAINT fk_account1 FOREIGN KEY (account1_id) REFERENCES account_details(id);
-- ALTER TABLE death_case ADD CONSTRAINT fk_account2 FOREIGN KEY (account2_id) REFERENCES account_details(id);
-- ALTER TABLE death_case ADD CONSTRAINT fk_account3 FOREIGN KEY (account3_id) REFERENCES account_details(id);
