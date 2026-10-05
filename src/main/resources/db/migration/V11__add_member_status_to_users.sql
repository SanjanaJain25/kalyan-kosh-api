-- Member tracking flag shared by all portal user databases.
-- This is intentionally separate from users.status (ACTIVE/BLOCKED/DELETED).
ALTER TABLE users
    ADD COLUMN member_status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    ADD COLUMN member_status_updated_at TIMESTAMP NULL,
    ADD COLUMN member_status_updated_by VARCHAR(20) NULL;

-- Make the default explicit for all existing rows as well.
UPDATE users
SET member_status = 'NORMAL'
WHERE member_status IS NULL OR TRIM(member_status) = '';
