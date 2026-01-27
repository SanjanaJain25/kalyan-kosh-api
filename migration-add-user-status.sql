-- ==========================================
-- ADD USER STATUS COLUMN TO USERS TABLE
-- ==========================================

-- Add status column to users table
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Create index for faster queries
CREATE INDEX idx_users_status ON users(status);

-- Update existing users to have ACTIVE status
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;

-- Verify the update
SELECT 
    status,
    COUNT(*) as count
FROM users
GROUP BY status;

-- Expected output:
-- +--------+-------+
-- | status | count |  
-- +--------+-------+
-- | ACTIVE | X     |
-- +--------+-------+