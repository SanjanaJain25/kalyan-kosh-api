-- ==========================================
-- FIX EXISTING USERS - ADD LOCATION DATA
-- ==========================================

-- First, let's see what we have
SELECT id, name, email,
       department_state_id,
       department_sambhag_id,
       department_district_id,
       department_block_id
FROM users;

-- Get location IDs
SELECT id, name, code FROM state;
SELECT id, name, state_id FROM sambhag;
SELECT id, name, sambhag_id FROM district;
SELECT id, name, district_id FROM block LIMIT 20;

-- Example: Update user with Indore location
-- Replace <state_id>, <sambhag_id>, <district_id>, <block_id> with actual IDs

-- Find Indore hierarchy IDs:
SELECT
    s.id as state_id,
    s.name as state_name,
    sa.id as sambhag_id,
    sa.name as sambhag_name,
    d.id as district_id,
    d.name as district_name,
    b.id as block_id,
    b.name as block_name
FROM state s
LEFT JOIN sambhag sa ON sa.state_id = s.id AND sa.name = 'इंदौर संभाग'
LEFT JOIN district d ON d.sambhag_id = sa.id AND d.name = 'इंदौर'
LEFT JOIN block b ON b.district_id = d.id AND b.name = 'इंदौर'
WHERE s.name = 'मध्य प्रदेश';

-- Now update users (replace UUIDs with actual IDs from above query)
-- UPDATE users
-- SET
--     department_state_id = '<state_id_from_above>',
--     department_sambhag_id = '<sambhag_id_from_above>',
--     department_district_id = '<district_id_from_above>',
--     department_block_id = '<block_id_from_above>'
-- WHERE id = 'PMUMS202458108';  -- Aman

-- Verify the update
SELECT
    u.id,
    u.name,
    s.name as state,
    sa.name as sambhag,
    d.name as district,
    b.name as block
FROM users u
LEFT JOIN state s ON u.department_state_id = s.id
LEFT JOIN sambhag sa ON u.department_sambhag_id = sa.id
LEFT JOIN district d ON u.department_district_id = d.id
LEFT JOIN block b ON u.department_block_id = b.id;

