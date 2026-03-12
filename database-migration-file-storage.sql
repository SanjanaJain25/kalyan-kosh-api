-- =====================================================
-- DATABASE MIGRATION: Add File Storage Columns
-- =====================================================
-- This migration adds columns to store file binary data
-- directly in MySQL database instead of local disk
-- =====================================================

USE kalyankosh_db;

-- Add columns for file storage
ALTER TABLE receipts
ADD COLUMN file_data LONGBLOB COMMENT 'Binary file data',
ADD COLUMN file_name VARCHAR(500) COMMENT 'Original filename',
ADD COLUMN file_type VARCHAR(100) COMMENT 'MIME type (e.g., image/jpeg)',
ADD COLUMN file_size BIGINT COMMENT 'File size in bytes';

-- Verify columns were added
DESCRIBE receipts;

-- Optional: Add index for better query performance
CREATE INDEX idx_receipt_file_name ON receipts(file_name);

-- Check existing data
SELECT id, file_path, file_name, file_size, LENGTH(file_data) as actual_size
FROM receipts
LIMIT 10;

-- =====================================================
-- MIGRATION COMPLETE
-- =====================================================
-- Next steps:
-- 1. Restart your Spring Boot application
-- 2. Test file upload via Postman
-- 3. Verify file is stored in database
-- =====================================================

