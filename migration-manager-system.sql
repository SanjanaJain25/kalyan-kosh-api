-- Create ManagerAssignment table for flexible manager-location assignments
CREATE TABLE IF NOT EXISTS manager_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Manager reference
    manager_id VARCHAR(255) NOT NULL,
    manager_level ENUM('SAMBHAG', 'DISTRICT', 'BLOCK') NOT NULL,
    
    -- Location assignments (nullable for flexibility)
    sambhag_id BIGINT NULL,
    district_id BIGINT NULL,
    block_id BIGINT NULL,
    
    -- Assignment metadata
    is_active BOOLEAN DEFAULT TRUE,
    assigned_by_id VARCHAR(255) NULL,
    notes TEXT NULL,
    
    -- Timestamps
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_manager_assignments_manager 
        FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_manager_assignments_assigned_by 
        FOREIGN KEY (assigned_by_id) REFERENCES users(id) ON DELETE SET NULL,
    
    CONSTRAINT fk_manager_assignments_sambhag 
        FOREIGN KEY (sambhag_id) REFERENCES sambhag(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_manager_assignments_district 
        FOREIGN KEY (district_id) REFERENCES district(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_manager_assignments_block 
        FOREIGN KEY (block_id) REFERENCES block(id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_manager_assignments_manager (manager_id),
    INDEX idx_manager_assignments_level (manager_level),
    INDEX idx_manager_assignments_sambhag (sambhag_id),
    INDEX idx_manager_assignments_district (district_id),
    INDEX idx_manager_assignments_block (block_id),
    INDEX idx_manager_assignments_active (is_active),
    
    -- Unique constraint to prevent duplicate active assignments
    UNIQUE INDEX idx_unique_active_assignment (manager_id, sambhag_id, district_id, block_id, is_active)
);

-- Create ManagerQuery table for query management system
CREATE TABLE IF NOT EXISTS manager_queries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Query content
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    
    -- Assignment and status
    created_by VARCHAR(255) NOT NULL,
    assigned_to VARCHAR(255) NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    status ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'ESCALATED') DEFAULT 'PENDING',
    
    -- Related context (optional)
    related_sambhag_id BIGINT NULL,
    related_district_id BIGINT NULL,
    related_block_id BIGINT NULL,
    related_user_id VARCHAR(255) NULL,
    
    -- Resolution
    resolution TEXT NULL,
    resolved_by VARCHAR(255) NULL,
    
    -- Timestamps
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_at DATETIME NULL,
    resolved_at DATETIME NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_manager_queries_created_by 
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_manager_queries_assigned_to 
        FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL,
    
    CONSTRAINT fk_manager_queries_resolved_by 
        FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,
    
    CONSTRAINT fk_manager_queries_sambhag 
        FOREIGN KEY (related_sambhag_id) REFERENCES sambhag(id) ON DELETE SET NULL,
    
    CONSTRAINT fk_manager_queries_district 
        FOREIGN KEY (related_district_id) REFERENCES district(id) ON DELETE SET NULL,
    
    CONSTRAINT fk_manager_queries_block 
        FOREIGN KEY (related_block_id) REFERENCES block(id) ON DELETE SET NULL,
    
    CONSTRAINT fk_manager_queries_related_user 
        FOREIGN KEY (related_user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Indexes for performance
    INDEX idx_manager_queries_created_by (created_by),
    INDEX idx_manager_queries_assigned_to (assigned_to),
    INDEX idx_manager_queries_status (status),
    INDEX idx_manager_queries_priority (priority),
    INDEX idx_manager_queries_created_at (created_at),
    INDEX idx_manager_queries_sambhag (related_sambhag_id),
    INDEX idx_manager_queries_district (related_district_id),
    INDEX idx_manager_queries_block (related_block_id),
    INDEX idx_manager_queries_related_user (related_user_id),
    
    -- Composite indexes for common queries
    INDEX idx_status_assigned (status, assigned_to),
    INDEX idx_priority_created_at (priority, created_at),
    INDEX idx_status_priority (status, priority)
);

-- Insert sample manager assignments (optional - for testing)
-- This assumes you have existing users with manager roles
/*
INSERT INTO manager_assignments (manager_id, manager_level, sambhag_id, assigned_by_id, notes) 
SELECT 
    u.id,
    'SAMBHAG',
    s.id,
    'admin_user_id', -- Replace with actual admin user ID
    CONCAT('Auto-assigned to manage ', s.name, ' sambhag')
FROM users u
JOIN sambhag s ON 1=1  -- Cross join to create sample assignments
WHERE u.role = 'ROLE_SAMBHAG_MANAGER'
LIMIT 10; -- Limit to prevent too many test records
*/