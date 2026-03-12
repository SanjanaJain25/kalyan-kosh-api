-- ============================================================
-- MP State Division District Block - Auto Update SQL
-- Generated from: mp_state_division_district_block.json
-- Date: January 8, 2026
-- ============================================================
-- This script will:
-- ✅ INSERT new records if they don't exist
-- ✅ UPDATE existing records if they already exist
-- ✅ Safe to run multiple times (idempotent)
-- ============================================================

-- Step 1: Add unique constraints if not exists (for upsert to work)
-- These are safe to run multiple times

-- Check and add unique constraint on states.name
SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'states' AND CONSTRAINT_NAME = 'uk_states_name');
SET @sql = IF(@constraint_exists = 0, 'ALTER TABLE states ADD CONSTRAINT uk_states_name UNIQUE (name)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add unique constraint on sambhag (name + state_id)
SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'sambhag' AND CONSTRAINT_NAME = 'uk_sambhag_name_state');
SET @sql = IF(@constraint_exists = 0, 'ALTER TABLE sambhag ADD CONSTRAINT uk_sambhag_name_state UNIQUE (name, state_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add unique constraint on districts (name + sambhag_id)
SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'districts' AND CONSTRAINT_NAME = 'uk_districts_name_sambhag');
SET @sql = IF(@constraint_exists = 0, 'ALTER TABLE districts ADD CONSTRAINT uk_districts_name_sambhag UNIQUE (name, sambhag_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add unique constraint on blocks (name + district_id)
SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'blocks' AND CONSTRAINT_NAME = 'uk_blocks_name_district');
SET @sql = IF(@constraint_exists = 0, 'ALTER TABLE blocks ADD CONSTRAINT uk_blocks_name_district UNIQUE (name, district_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '✅ Unique constraints verified/added' AS Status;

-- ============================================================
-- Step 2: Insert/Update State
-- ============================================================
INSERT INTO states (name, code) VALUES ('Madhya Pradesh', 'MP')
ON DUPLICATE KEY UPDATE code = VALUES(code);

SET @mp_state_id = (SELECT id FROM states WHERE name = 'Madhya Pradesh');
SELECT CONCAT('✅ State: Madhya Pradesh (ID: ', @mp_state_id, ')') AS Status;

-- ============================================================
-- Step 3: Create Stored Procedure for Upsert Operations
-- ============================================================
DROP PROCEDURE IF EXISTS upsert_sambhag;
DROP PROCEDURE IF EXISTS upsert_district;
DROP PROCEDURE IF EXISTS upsert_block;

DELIMITER //

CREATE PROCEDURE upsert_sambhag(IN p_name VARCHAR(255), IN p_state_id BIGINT)
BEGIN
    INSERT INTO sambhag (name, state_id) VALUES (p_name, p_state_id)
    ON DUPLICATE KEY UPDATE name = VALUES(name);
END //

CREATE PROCEDURE upsert_district(IN p_name VARCHAR(255), IN p_sambhag_name VARCHAR(255))
BEGIN
    DECLARE v_sambhag_id BIGINT;
    SELECT id INTO v_sambhag_id FROM sambhag WHERE name = p_sambhag_name LIMIT 1;
    IF v_sambhag_id IS NOT NULL THEN
        INSERT INTO districts (name, sambhag_id) VALUES (p_name, v_sambhag_id)
        ON DUPLICATE KEY UPDATE name = VALUES(name);
    END IF;
END //

CREATE PROCEDURE upsert_block(IN p_name VARCHAR(255), IN p_district_name VARCHAR(255), IN p_sambhag_name VARCHAR(255))
BEGIN
    DECLARE v_district_id BIGINT;
    DECLARE v_sambhag_id BIGINT;
    SELECT id INTO v_sambhag_id FROM sambhag WHERE name = p_sambhag_name LIMIT 1;
    SELECT id INTO v_district_id FROM districts WHERE name = p_district_name AND sambhag_id = v_sambhag_id LIMIT 1;
    IF v_district_id IS NOT NULL THEN
        INSERT INTO blocks (name, district_id) VALUES (p_name, v_district_id)
        ON DUPLICATE KEY UPDATE name = VALUES(name);
    END IF;
END //

DELIMITER ;

SELECT '✅ Stored procedures created' AS Status;

-- ============================================================
-- UJJAIN SAMBHAG
-- ============================================================
CALL upsert_sambhag('Ujjain', @mp_state_id);

-- Agar Malwa District
CALL upsert_district('Agar Malwa', 'Ujjain');
CALL upsert_block('Barod', 'Agar Malwa', 'Ujjain');
CALL upsert_block('Nalkheda', 'Agar Malwa', 'Ujjain');
CALL upsert_block('Susner', 'Agar Malwa', 'Ujjain');
CALL upsert_block('Agar', 'Agar Malwa', 'Ujjain');

-- Dewas District
CALL upsert_district('Dewas', 'Ujjain');
CALL upsert_block('Dewas', 'Dewas', 'Ujjain');
CALL upsert_block('Tonkkhurd', 'Dewas', 'Ujjain');
CALL upsert_block('Sonkatch', 'Dewas', 'Ujjain');
CALL upsert_block('Bagli', 'Dewas', 'Ujjain');
CALL upsert_block('Kannod', 'Dewas', 'Ujjain');
CALL upsert_block('Khategaon', 'Dewas', 'Ujjain');

-- Mandsaur District
CALL upsert_district('Mandsaur', 'Ujjain');
CALL upsert_block('Bhanpura', 'Mandsaur', 'Ujjain');
CALL upsert_block('Garoth', 'Mandsaur', 'Ujjain');
CALL upsert_block('Malhargarh', 'Mandsaur', 'Ujjain');
CALL upsert_block('Mandsaur', 'Mandsaur', 'Ujjain');
CALL upsert_block('Sitamau', 'Mandsaur', 'Ujjain');

-- Neemuch District
CALL upsert_district('Neemuch', 'Ujjain');
CALL upsert_block('Jawad', 'Neemuch', 'Ujjain');
CALL upsert_block('Manasa', 'Neemuch', 'Ujjain');
CALL upsert_block('Neemuch', 'Neemuch', 'Ujjain');

-- Ratlam District
CALL upsert_district('Ratlam', 'Ujjain');
CALL upsert_block('Alot', 'Ratlam', 'Ujjain');
CALL upsert_block('Bajna', 'Ratlam', 'Ujjain');
CALL upsert_block('Jaora', 'Ratlam', 'Ujjain');
CALL upsert_block('Piploda', 'Ratlam', 'Ujjain');
CALL upsert_block('Ratlam', 'Ratlam', 'Ujjain');
CALL upsert_block('Sailana', 'Ratlam', 'Ujjain');

-- Shajapur District
CALL upsert_district('Shajapur', 'Ujjain');
CALL upsert_block('M Barodiya', 'Shajapur', 'Ujjain');
CALL upsert_block('Kalapipal', 'Shajapur', 'Ujjain');
CALL upsert_block('Shajapur', 'Shajapur', 'Ujjain');
CALL upsert_block('Shujalpur', 'Shajapur', 'Ujjain');

-- Ujjain District
CALL upsert_district('Ujjain', 'Ujjain');
CALL upsert_block('Ujjain Rural', 'Ujjain', 'Ujjain');
CALL upsert_block('Ghatiya', 'Ujjain', 'Ujjain');
CALL upsert_block('Badnagar', 'Ujjain', 'Ujjain');
CALL upsert_block('Khachrod', 'Ujjain', 'Ujjain');
CALL upsert_block('Tarana', 'Ujjain', 'Ujjain');
CALL upsert_block('Mahidpur', 'Ujjain', 'Ujjain');
CALL upsert_block('Ujjain URBAN', 'Ujjain', 'Ujjain');

SELECT '✅ Ujjain Sambhag completed' AS Status;

-- ============================================================
-- INDORE SAMBHAG
-- ============================================================
CALL upsert_sambhag('Indore', @mp_state_id);

-- Alirajpur District
CALL upsert_district('Alirajpur', 'Indore');
CALL upsert_block('Jobat', 'Alirajpur', 'Indore');
CALL upsert_block('Alirajpur', 'Alirajpur', 'Indore');
CALL upsert_block('Katthiwara', 'Alirajpur', 'Indore');
CALL upsert_block('Udaygarh', 'Alirajpur', 'Indore');
CALL upsert_block('Sondwa', 'Alirajpur', 'Indore');
CALL upsert_block('Bhabra', 'Alirajpur', 'Indore');

-- Barwani District
CALL upsert_district('Barwani', 'Indore');
CALL upsert_block('Raj Pur', 'Barwani', 'Indore');
CALL upsert_block('Barwani', 'Barwani', 'Indore');
CALL upsert_block('Pati', 'Barwani', 'Indore');
CALL upsert_block('Sendhwa', 'Barwani', 'Indore');
CALL upsert_block('Pansemal', 'Barwani', 'Indore');
CALL upsert_block('Niwali', 'Barwani', 'Indore');
CALL upsert_block('Thikri', 'Barwani', 'Indore');

-- Burhanpur District
CALL upsert_district('Burhanpur', 'Indore');
CALL upsert_block('Burhanpur', 'Burhanpur', 'Indore');
CALL upsert_block('Khaknar', 'Burhanpur', 'Indore');

-- Dhar District
CALL upsert_district('Dhar', 'Indore');
CALL upsert_block('Badnavar', 'Dhar', 'Indore');
CALL upsert_block('Bagh', 'Dhar', 'Indore');
CALL upsert_block('Dahi', 'Dhar', 'Indore');
CALL upsert_block('Dhar', 'Dhar', 'Indore');
CALL upsert_block('Dharampuri', 'Dhar', 'Indore');
CALL upsert_block('Gandhwani', 'Dhar', 'Indore');
CALL upsert_block('Kukshi', 'Dhar', 'Indore');
CALL upsert_block('Manavar', 'Dhar', 'Indore');
CALL upsert_block('Nalchha', 'Dhar', 'Indore');
CALL upsert_block('Nisarpur', 'Dhar', 'Indore');
CALL upsert_block('Sardarpur', 'Dhar', 'Indore');
CALL upsert_block('Tirla', 'Dhar', 'Indore');
CALL upsert_block('Umarban', 'Dhar', 'Indore');

-- Indore District
CALL upsert_district('Indore', 'Indore');
CALL upsert_block('Indore URBAN- 1', 'Indore', 'Indore');
CALL upsert_block('Mhow', 'Indore', 'Indore');
CALL upsert_block('Depalpur', 'Indore', 'Indore');
CALL upsert_block('Sanwer', 'Indore', 'Indore');
CALL upsert_block('Indore Rural', 'Indore', 'Indore');
CALL upsert_block('Indore URBAN- 2', 'Indore', 'Indore');

-- Jhabua District
CALL upsert_district('Jhabua', 'Indore');
CALL upsert_block('Thandla', 'Jhabua', 'Indore');
CALL upsert_block('Jhabua', 'Jhabua', 'Indore');
CALL upsert_block('Petlawad', 'Jhabua', 'Indore');
CALL upsert_block('Rama', 'Jhabua', 'Indore');
CALL upsert_block('Meghnagar', 'Jhabua', 'Indore');
CALL upsert_block('Ranapur', 'Jhabua', 'Indore');

-- Khandwa District
CALL upsert_district('Khandwa', 'Indore');
CALL upsert_block('Baladi', 'Khandwa', 'Indore');
CALL upsert_block('Chhaigaonmakhan', 'Khandwa', 'Indore');
CALL upsert_block('Harsud', 'Khandwa', 'Indore');
CALL upsert_block('Khalwa', 'Khandwa', 'Indore');
CALL upsert_block('Khandwa', 'Khandwa', 'Indore');
CALL upsert_block('Pandhana', 'Khandwa', 'Indore');
CALL upsert_block('Punasa', 'Khandwa', 'Indore');

-- Khargone District
CALL upsert_district('Khargone', 'Indore');
CALL upsert_block('Segaon', 'Khargone', 'Indore');
CALL upsert_block('Bhagwanpura', 'Khargone', 'Indore');
CALL upsert_block('Bhikangaon', 'Khargone', 'Indore');
CALL upsert_block('Jhirniya', 'Khargone', 'Indore');
CALL upsert_block('Gogawa', 'Khargone', 'Indore');
CALL upsert_block('Kasrawad', 'Khargone', 'Indore');
CALL upsert_block('Badwaha', 'Khargone', 'Indore');
CALL upsert_block('Maheshwar', 'Khargone', 'Indore');
CALL upsert_block('Khargone', 'Khargone', 'Indore');

SELECT '✅ Indore Sambhag completed' AS Status;

-- ============================================================
-- SHAHDOL SAMBHAG
-- ============================================================
CALL upsert_sambhag('Shahdol', @mp_state_id);

-- Anuppur District
CALL upsert_district('Anuppur', 'Shahdol');
CALL upsert_block('Anuppur', 'Anuppur', 'Shahdol');
CALL upsert_block('Jaithari', 'Anuppur', 'Shahdol');
CALL upsert_block('Kotma', 'Anuppur', 'Shahdol');
CALL upsert_block('Pushpraj Garh', 'Anuppur', 'Shahdol');

-- Shahdol District
CALL upsert_district('Shahdol', 'Shahdol');
CALL upsert_block('Beohari', 'Shahdol', 'Shahdol');
CALL upsert_block('Burhar', 'Shahdol', 'Shahdol');
CALL upsert_block('Gohparu', 'Shahdol', 'Shahdol');
CALL upsert_block('Jaisinghnagar', 'Shahdol', 'Shahdol');
CALL upsert_block('Sohagpur', 'Shahdol', 'Shahdol');

-- Umaria District
CALL upsert_district('Umaria', 'Shahdol');
CALL upsert_block('Karkeli', 'Umaria', 'Shahdol');
CALL upsert_block('Manpur', 'Umaria', 'Shahdol');
CALL upsert_block('Pali', 'Umaria', 'Shahdol');

SELECT '✅ Shahdol Sambhag completed' AS Status;

-- ============================================================
-- GWALIOR SAMBHAG
-- ============================================================
CALL upsert_sambhag('Gwalior', @mp_state_id);

-- Ashoknagar District
CALL upsert_district('Ashoknagar', 'Gwalior');
CALL upsert_block('Ashoknagar', 'Ashoknagar', 'Gwalior');
CALL upsert_block('Chanderi', 'Ashoknagar', 'Gwalior');
CALL upsert_block('Isagarh', 'Ashoknagar', 'Gwalior');
CALL upsert_block('Mugawali', 'Ashoknagar', 'Gwalior');

-- Datia District
CALL upsert_district('Datia', 'Gwalior');
CALL upsert_block('Datia', 'Datia', 'Gwalior');
CALL upsert_block('Seondha', 'Datia', 'Gwalior');
CALL upsert_block('Bhander', 'Datia', 'Gwalior');

-- Guna District
CALL upsert_district('Guna', 'Gwalior');
CALL upsert_block('Aron', 'Guna', 'Gwalior');
CALL upsert_block('Bamori', 'Guna', 'Gwalior');
CALL upsert_block('Chachoda', 'Guna', 'Gwalior');
CALL upsert_block('Guna', 'Guna', 'Gwalior');
CALL upsert_block('Raghogarh', 'Guna', 'Gwalior');

-- Gwalior District
CALL upsert_district('Gwalior', 'Gwalior');
CALL upsert_block('Bhitarvar', 'Gwalior', 'Gwalior');
CALL upsert_block('Dabra', 'Gwalior', 'Gwalior');
CALL upsert_block('Ghatigaon', 'Gwalior', 'Gwalior');
CALL upsert_block('Murar rural', 'Gwalior', 'Gwalior');
CALL upsert_block('Murar Urban 1', 'Gwalior', 'Gwalior');
CALL upsert_block('Murar Urban 2', 'Gwalior', 'Gwalior');

-- Shivpuri District
CALL upsert_district('Shivpuri', 'Gwalior');
CALL upsert_block('Shivpuri', 'Shivpuri', 'Gwalior');
CALL upsert_block('Kolaras', 'Shivpuri', 'Gwalior');
CALL upsert_block('Badarwas', 'Shivpuri', 'Gwalior');
CALL upsert_block('Khaniyadhana', 'Shivpuri', 'Gwalior');
CALL upsert_block('Pohri', 'Shivpuri', 'Gwalior');
CALL upsert_block('Karaira', 'Shivpuri', 'Gwalior');
CALL upsert_block('Narvar', 'Shivpuri', 'Gwalior');
CALL upsert_block('Pichhore', 'Shivpuri', 'Gwalior');

SELECT '✅ Gwalior Sambhag completed' AS Status;

-- ============================================================
-- JABALPUR SAMBHAG
-- ============================================================
CALL upsert_sambhag('Jabalpur', @mp_state_id);

-- Balaghat District
CALL upsert_district('Balaghat', 'Jabalpur');
CALL upsert_block('Balaghat', 'Balaghat', 'Jabalpur');
CALL upsert_block('Kirnapur', 'Balaghat', 'Jabalpur');
CALL upsert_block('Katangi', 'Balaghat', 'Jabalpur');
CALL upsert_block('Khairlanji', 'Balaghat', 'Jabalpur');
CALL upsert_block('Paraswada', 'Balaghat', 'Jabalpur');
CALL upsert_block('LalBarra', 'Balaghat', 'Jabalpur');
CALL upsert_block('WaraSeoni', 'Balaghat', 'Jabalpur');
CALL upsert_block('Lanji', 'Balaghat', 'Jabalpur');
CALL upsert_block('Baihar', 'Balaghat', 'Jabalpur');
CALL upsert_block('Birsa', 'Balaghat', 'Jabalpur');

-- Chhindwara District
CALL upsert_district('Chhindwara', 'Jabalpur');
CALL upsert_block('Chhindwara', 'Chhindwara', 'Jabalpur');
CALL upsert_block('Mohkhed', 'Chhindwara', 'Jabalpur');
CALL upsert_block('Parasia', 'Chhindwara', 'Jabalpur');
CALL upsert_block('Junnardeo', 'Chhindwara', 'Jabalpur');
CALL upsert_block('Tamia', 'Chhindwara', 'Jabalpur');
CALL upsert_block('Bichhua', 'Chhindwara', 'Jabalpur');
CALL upsert_block('Amarwada', 'Chhindwara', 'Jabalpur');
CALL upsert_block('Harrai', 'Chhindwara', 'Jabalpur');
CALL upsert_block('Chourai', 'Chhindwara', 'Jabalpur');

-- Dindori District
CALL upsert_district('Dindori', 'Jabalpur');
CALL upsert_block('Dindori', 'Dindori', 'Jabalpur');
CALL upsert_block('Amarpur', 'Dindori', 'Jabalpur');
CALL upsert_block('Karanjia', 'Dindori', 'Jabalpur');
CALL upsert_block('Samnapur', 'Dindori', 'Jabalpur');
CALL upsert_block('Bajag', 'Dindori', 'Jabalpur');
CALL upsert_block('Mehandwani', 'Dindori', 'Jabalpur');
CALL upsert_block('Shahpura', 'Dindori', 'Jabalpur');

-- Jabalpur District
CALL upsert_district('Jabalpur', 'Jabalpur');
CALL upsert_block('Jabalpur Rural', 'Jabalpur', 'Jabalpur');
CALL upsert_block('Jabalpur URBAN- 1', 'Jabalpur', 'Jabalpur');
CALL upsert_block('Majholi', 'Jabalpur', 'Jabalpur');
CALL upsert_block('Panagar', 'Jabalpur', 'Jabalpur');
CALL upsert_block('Patan', 'Jabalpur', 'Jabalpur');
CALL upsert_block('Shahpura', 'Jabalpur', 'Jabalpur');
CALL upsert_block('Sihora', 'Jabalpur', 'Jabalpur');
CALL upsert_block('Kundam', 'Jabalpur', 'Jabalpur');
CALL upsert_block('Jabalpur URBAN- 2', 'Jabalpur', 'Jabalpur');

-- Katni District
CALL upsert_district('Katni', 'Jabalpur');
CALL upsert_block('Bahoriband', 'Katni', 'Jabalpur');
CALL upsert_block('Katni', 'Katni', 'Jabalpur');
CALL upsert_block('Badwara', 'Katni', 'Jabalpur');
CALL upsert_block('Rithi', 'Katni', 'Jabalpur');
CALL upsert_block('Vijay Raghavgarh', 'Katni', 'Jabalpur');
CALL upsert_block('Dheemar Kheda', 'Katni', 'Jabalpur');

-- Mandla District
CALL upsert_district('Mandla', 'Jabalpur');
CALL upsert_block('Mandla', 'Mandla', 'Jabalpur');
CALL upsert_block('Nainpur', 'Mandla', 'Jabalpur');
CALL upsert_block('Bichhia', 'Mandla', 'Jabalpur');
CALL upsert_block('Mohgaon', 'Mandla', 'Jabalpur');
CALL upsert_block('Mawai', 'Mandla', 'Jabalpur');
CALL upsert_block('Ghughri', 'Mandla', 'Jabalpur');
CALL upsert_block('Niwas', 'Mandla', 'Jabalpur');
CALL upsert_block('Narayanganj', 'Mandla', 'Jabalpur');
CALL upsert_block('Beejadandi', 'Mandla', 'Jabalpur');

-- Narsinghpur District
CALL upsert_district('Narsinghpur', 'Jabalpur');
CALL upsert_block('Narsinghpur', 'Narsinghpur', 'Jabalpur');
CALL upsert_block('Chichli', 'Narsinghpur', 'Jabalpur');
CALL upsert_block('Gotegaon Shridham', 'Narsinghpur', 'Jabalpur');
CALL upsert_block('Kareli', 'Narsinghpur', 'Jabalpur');
CALL upsert_block('Saikheda', 'Narsinghpur', 'Jabalpur');
CALL upsert_block('Chawarpatha', 'Narsinghpur', 'Jabalpur');

-- Pandhurna District
CALL upsert_district('Pandhurna', 'Jabalpur');
CALL upsert_block('Sausar', 'Pandhurna', 'Jabalpur');
CALL upsert_block('Pandhurna', 'Pandhurna', 'Jabalpur');

-- Seoni District
CALL upsert_district('Seoni', 'Jabalpur');
CALL upsert_block('Seoni', 'Seoni', 'Jabalpur');
CALL upsert_block('Barghat', 'Seoni', 'Jabalpur');
CALL upsert_block('Keolari', 'Seoni', 'Jabalpur');
CALL upsert_block('Kurai', 'Seoni', 'Jabalpur');
CALL upsert_block('Chhapara', 'Seoni', 'Jabalpur');
CALL upsert_block('Lakhnadon', 'Seoni', 'Jabalpur');
CALL upsert_block('Ghansore', 'Seoni', 'Jabalpur');
CALL upsert_block('Dhanora', 'Seoni', 'Jabalpur');

SELECT '✅ Jabalpur Sambhag completed' AS Status;

-- ============================================================
-- NARMADAPURAM SAMBHAG
-- ============================================================
CALL upsert_sambhag('Narmadapuram', @mp_state_id);

-- Betul District
CALL upsert_district('Betul', 'Narmadapuram');
CALL upsert_block('Amla', 'Betul', 'Narmadapuram');
CALL upsert_block('Athner', 'Betul', 'Narmadapuram');
CALL upsert_block('Betul', 'Betul', 'Narmadapuram');
CALL upsert_block('Bhainsdehi', 'Betul', 'Narmadapuram');
CALL upsert_block('Bhimpur', 'Betul', 'Narmadapuram');
CALL upsert_block('Chicholi', 'Betul', 'Narmadapuram');
CALL upsert_block('Ghoradongri', 'Betul', 'Narmadapuram');
CALL upsert_block('Multai', 'Betul', 'Narmadapuram');
CALL upsert_block('Prabhat Pattan', 'Betul', 'Narmadapuram');
CALL upsert_block('Shahpur', 'Betul', 'Narmadapuram');

-- Harda District
CALL upsert_district('Harda', 'Narmadapuram');
CALL upsert_block('Harda', 'Harda', 'Narmadapuram');
CALL upsert_block('Timarni', 'Harda', 'Narmadapuram');
CALL upsert_block('Khirkiya', 'Harda', 'Narmadapuram');

-- Narmadapuram District
CALL upsert_district('Narmadapuram', 'Narmadapuram');
CALL upsert_block('Bankhedi', 'Narmadapuram', 'Narmadapuram');
CALL upsert_block('Babai (Makhannagar)', 'Narmadapuram', 'Narmadapuram');
CALL upsert_block('Narmadapuram', 'Narmadapuram', 'Narmadapuram');
CALL upsert_block('Kesla', 'Narmadapuram', 'Narmadapuram');
CALL upsert_block('Sohaghpur', 'Narmadapuram', 'Narmadapuram');
CALL upsert_block('Pipariya', 'Narmadapuram', 'Narmadapuram');
CALL upsert_block('Sivni Maalva', 'Narmadapuram', 'Narmadapuram');

SELECT '✅ Narmadapuram Sambhag completed' AS Status;

-- ============================================================
-- CHAMBAL SAMBHAG
-- ============================================================
CALL upsert_sambhag('Chambal', @mp_state_id);

-- Bhind District
CALL upsert_district('Bhind', 'Chambal');
CALL upsert_block('Ater', 'Bhind', 'Chambal');
CALL upsert_block('Bhind', 'Bhind', 'Chambal');
CALL upsert_block('Gohad', 'Bhind', 'Chambal');
CALL upsert_block('Lahar', 'Bhind', 'Chambal');
CALL upsert_block('Mehgaon', 'Bhind', 'Chambal');
CALL upsert_block('Roun', 'Bhind', 'Chambal');

-- Morena District
CALL upsert_district('Morena', 'Chambal');
CALL upsert_block('Ambah', 'Morena', 'Chambal');
CALL upsert_block('Joura', 'Morena', 'Chambal');
CALL upsert_block('Kailaras', 'Morena', 'Chambal');
CALL upsert_block('Pahadgarh', 'Morena', 'Chambal');
CALL upsert_block('Porsa', 'Morena', 'Chambal');
CALL upsert_block('Sabalgarh', 'Morena', 'Chambal');
CALL upsert_block('Morena', 'Morena', 'Chambal');

-- Sheopur District
CALL upsert_district('Sheopur', 'Chambal');
CALL upsert_block('Vijaypur', 'Sheopur', 'Chambal');
CALL upsert_block('Sheopur', 'Sheopur', 'Chambal');
CALL upsert_block('Karahal', 'Sheopur', 'Chambal');

SELECT '✅ Chambal Sambhag completed' AS Status;

-- ============================================================
-- BHOPAL SAMBHAG
-- ============================================================
CALL upsert_sambhag('Bhopal', @mp_state_id);

-- Bhopal District
CALL upsert_district('Bhopal', 'Bhopal');
CALL upsert_block('Berasia', 'Bhopal', 'Bhopal');
CALL upsert_block('Phanda Gramin', 'Bhopal', 'Bhopal');
CALL upsert_block('Phanda URBAN- New City', 'Bhopal', 'Bhopal');
CALL upsert_block('Phanda URBAN- Old City', 'Bhopal', 'Bhopal');

-- Raisen District
CALL upsert_district('Raisen', 'Bhopal');
CALL upsert_block('Badi', 'Raisen', 'Bhopal');
CALL upsert_block('Begamganj', 'Raisen', 'Bhopal');
CALL upsert_block('Gairatganj', 'Raisen', 'Bhopal');
CALL upsert_block('Obedullaganj', 'Raisen', 'Bhopal');
CALL upsert_block('Sanchi', 'Raisen', 'Bhopal');
CALL upsert_block('Silwani', 'Raisen', 'Bhopal');
CALL upsert_block('Udaipura', 'Raisen', 'Bhopal');

-- Rajgarh District
CALL upsert_district('Rajgarh', 'Bhopal');
CALL upsert_block('Rajgarh', 'Rajgarh', 'Bhopal');
CALL upsert_block('Biaora', 'Rajgarh', 'Bhopal');
CALL upsert_block('Narsinghgarh', 'Rajgarh', 'Bhopal');
CALL upsert_block('Sarangpur', 'Rajgarh', 'Bhopal');
CALL upsert_block('Khilchipur', 'Rajgarh', 'Bhopal');
CALL upsert_block('Zirapur', 'Rajgarh', 'Bhopal');

-- Sehore District
CALL upsert_district('Sehore', 'Bhopal');
CALL upsert_block('Ashta', 'Sehore', 'Bhopal');
CALL upsert_block('Budhani', 'Sehore', 'Bhopal');
CALL upsert_block('Ichhawar', 'Sehore', 'Bhopal');
CALL upsert_block('Nasrullaganj (Bhairunda)', 'Sehore', 'Bhopal');
CALL upsert_block('Sehore', 'Sehore', 'Bhopal');

-- Vidisha District
CALL upsert_district('Vidisha', 'Bhopal');
CALL upsert_block('Basoda', 'Vidisha', 'Bhopal');
CALL upsert_block('Gyaraspur', 'Vidisha', 'Bhopal');
CALL upsert_block('Kurwai', 'Vidisha', 'Bhopal');
CALL upsert_block('Lateri', 'Vidisha', 'Bhopal');
CALL upsert_block('Nateran', 'Vidisha', 'Bhopal');
CALL upsert_block('Sironj', 'Vidisha', 'Bhopal');
CALL upsert_block('Vidisha', 'Vidisha', 'Bhopal');

SELECT '✅ Bhopal Sambhag completed' AS Status;

-- ============================================================
-- SAGAR SAMBHAG
-- ============================================================
CALL upsert_sambhag('Sagar', @mp_state_id);

-- Chhatarpur District
CALL upsert_district('Chhatarpur', 'Sagar');
CALL upsert_block('Badamalahara', 'Chhatarpur', 'Sagar');
CALL upsert_block('Buxwaha', 'Chhatarpur', 'Sagar');
CALL upsert_block('Bijawar', 'Chhatarpur', 'Sagar');
CALL upsert_block('Barigarh/Gaurihar', 'Chhatarpur', 'Sagar');
CALL upsert_block('Chhatarpur/Ishanagar', 'Chhatarpur', 'Sagar');
CALL upsert_block('Laundi', 'Chhatarpur', 'Sagar');
CALL upsert_block('Nowgong', 'Chhatarpur', 'Sagar');
CALL upsert_block('Rajnagar', 'Chhatarpur', 'Sagar');

-- Damoh District
CALL upsert_district('Damoh', 'Sagar');
CALL upsert_block('Patera', 'Damoh', 'Sagar');
CALL upsert_block('Patharia', 'Damoh', 'Sagar');
CALL upsert_block('Damoh', 'Damoh', 'Sagar');
CALL upsert_block('Hatta', 'Damoh', 'Sagar');
CALL upsert_block('Tendukheda', 'Damoh', 'Sagar');
CALL upsert_block('Jabera', 'Damoh', 'Sagar');
CALL upsert_block('Batiyagarh', 'Damoh', 'Sagar');

-- Niwari District
CALL upsert_district('Niwari', 'Sagar');
CALL upsert_block('Niwari', 'Niwari', 'Sagar');
CALL upsert_block('Prathvipur', 'Niwari', 'Sagar');

-- Panna District
CALL upsert_district('Panna', 'Sagar');
CALL upsert_block('Ajaigarh', 'Panna', 'Sagar');
CALL upsert_block('Gunour', 'Panna', 'Sagar');
CALL upsert_block('Panna', 'Panna', 'Sagar');
CALL upsert_block('Pawai', 'Panna', 'Sagar');
CALL upsert_block('Shahnagar', 'Panna', 'Sagar');

-- Sagar District
CALL upsert_district('Sagar', 'Sagar');
CALL upsert_block('Malthon', 'Sagar', 'Sagar');
CALL upsert_block('Jaisinagar', 'Sagar', 'Sagar');
CALL upsert_block('Shahgarh', 'Sagar', 'Sagar');
CALL upsert_block('Bina', 'Sagar', 'Sagar');
CALL upsert_block('Khurai', 'Sagar', 'Sagar');
CALL upsert_block('Deori', 'Sagar', 'Sagar');
CALL upsert_block('Kesli', 'Sagar', 'Sagar');
CALL upsert_block('Rahatgarh', 'Sagar', 'Sagar');
CALL upsert_block('Banda', 'Sagar', 'Sagar');
CALL upsert_block('Sagar', 'Sagar', 'Sagar');
CALL upsert_block('Rehli', 'Sagar', 'Sagar');

-- Tikamgarh District
CALL upsert_district('Tikamgarh', 'Sagar');
CALL upsert_block('Baldewgarh', 'Tikamgarh', 'Sagar');
CALL upsert_block('Jatara', 'Tikamgarh', 'Sagar');
CALL upsert_block('Palera', 'Tikamgarh', 'Sagar');
CALL upsert_block('Tikamgarh', 'Tikamgarh', 'Sagar');

SELECT '✅ Sagar Sambhag completed' AS Status;

-- ============================================================
-- REWA SAMBHAG
-- ============================================================
CALL upsert_sambhag('Rewa', @mp_state_id);

-- Maihar District
CALL upsert_district('Maihar', 'Rewa');
CALL upsert_block('Amarpatan', 'Maihar', 'Rewa');
CALL upsert_block('Maihar', 'Maihar', 'Rewa');
CALL upsert_block('Ram Nagar', 'Maihar', 'Rewa');

-- Mauganj District
CALL upsert_district('Mauganj', 'Rewa');
CALL upsert_block('Mauganj', 'Mauganj', 'Rewa');
CALL upsert_block('Naigarhi', 'Mauganj', 'Rewa');
CALL upsert_block('Hanumana', 'Mauganj', 'Rewa');

-- Rewa District
CALL upsert_district('Rewa', 'Rewa');
CALL upsert_block('Rewa', 'Rewa', 'Rewa');
CALL upsert_block('Raipur (K)', 'Rewa', 'Rewa');
CALL upsert_block('Sirmour', 'Rewa', 'Rewa');
CALL upsert_block('Teonthar', 'Rewa', 'Rewa');
CALL upsert_block('Gangeo', 'Rewa', 'Rewa');
CALL upsert_block('Jawa', 'Rewa', 'Rewa');

-- Satna District
CALL upsert_district('Satna', 'Rewa');
CALL upsert_block('Majhgwan', 'Satna', 'Rewa');
CALL upsert_block('Nagod', 'Satna', 'Rewa');
CALL upsert_block('Rampur Baghelan', 'Satna', 'Rewa');
CALL upsert_block('Satna (Sohwal)', 'Satna', 'Rewa');
CALL upsert_block('Unchera', 'Satna', 'Rewa');

-- Sidhi District
CALL upsert_district('Sidhi', 'Rewa');
CALL upsert_block('Kusami', 'Sidhi', 'Rewa');
CALL upsert_block('Majhauli', 'Sidhi', 'Rewa');
CALL upsert_block('Rampur Naikin', 'Sidhi', 'Rewa');
CALL upsert_block('Sidhi', 'Sidhi', 'Rewa');
CALL upsert_block('Sihawal', 'Sidhi', 'Rewa');

-- Singrauli District
CALL upsert_district('Singrauli', 'Rewa');
CALL upsert_block('Chitarangi', 'Singrauli', 'Rewa');
CALL upsert_block('Deosar', 'Singrauli', 'Rewa');
CALL upsert_block('Waidhan', 'Singrauli', 'Rewa');

SELECT '✅ Rewa Sambhag completed' AS Status;

-- ============================================================
-- CLEANUP: Drop Stored Procedures
-- ============================================================
DROP PROCEDURE IF EXISTS upsert_sambhag;
DROP PROCEDURE IF EXISTS upsert_district;
DROP PROCEDURE IF EXISTS upsert_block;

-- ============================================================
-- SUMMARY
-- ============================================================
SELECT '============================================' AS '';
SELECT '✅ MP LOCATION DATA SYNC COMPLETE!' AS Status;
SELECT '============================================' AS '';
SELECT CONCAT('States: ', COUNT(*)) AS Summary FROM states;
SELECT CONCAT('Sambhags: ', COUNT(*)) AS Summary FROM sambhag;
SELECT CONCAT('Districts: ', COUNT(*)) AS Summary FROM districts;
SELECT CONCAT('Blocks: ', COUNT(*)) AS Summary FROM blocks;
SELECT '============================================' AS '';

