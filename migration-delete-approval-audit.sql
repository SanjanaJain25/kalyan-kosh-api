-- =========================================================
-- DELETE REQUESTS
-- =========================================================
CREATE TABLE IF NOT EXISTS delete_requests (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    requested_by VARCHAR(20) NOT NULL,
    requested_by_role VARCHAR(50) NOT NULL,
    requested_from_dashboard VARCHAR(50) NULL,
    reason VARCHAR(500) NULL,
    status VARCHAR(50) NOT NULL,
    approval_level VARCHAR(50) NULL,

    approved_by VARCHAR(20) NULL,
    approved_at DATETIME NULL,

    rejected_by VARCHAR(20) NULL,
    rejected_at DATETIME NULL,
    rejection_reason VARCHAR(500) NULL,

    executed_at DATETIME NULL,

    restore_requested_by VARCHAR(20) NULL,
    restore_approved_by VARCHAR(20) NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    INDEX idx_delete_requests_entity (entity_type, entity_id),
    INDEX idx_delete_requests_status (status),
    INDEX idx_delete_requests_requested_by (requested_by),
    CONSTRAINT fk_delete_requests_requested_by FOREIGN KEY (requested_by) REFERENCES users(id),
    CONSTRAINT fk_delete_requests_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT fk_delete_requests_rejected_by FOREIGN KEY (rejected_by) REFERENCES users(id),
    CONSTRAINT fk_delete_requests_restore_requested_by FOREIGN KEY (restore_requested_by) REFERENCES users(id),
    CONSTRAINT fk_delete_requests_restore_approved_by FOREIGN KEY (restore_approved_by) REFERENCES users(id)
);

-- =========================================================
-- AUDIT LOGS
-- =========================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    action_type VARCHAR(50) NOT NULL,

    old_data_json LONGTEXT NULL,
    new_data_json LONGTEXT NULL,

    performed_by VARCHAR(20) NULL,
    performed_by_role VARCHAR(50) NULL,
    remarks VARCHAR(1000) NULL,
    ip_address VARCHAR(100) NULL,

    performed_at DATETIME NOT NULL,

    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_action (action_type),
    INDEX idx_audit_logs_performed_by (performed_by),
    CONSTRAINT fk_audit_logs_performed_by FOREIGN KEY (performed_by) REFERENCES users(id)
);

-- =========================================================
-- USERS TABLE ADDITIONAL SOFT DELETE METADATA
-- =========================================================
ALTER TABLE users
    ADD COLUMN deleted_at DATETIME NULL,
    ADD COLUMN deleted_by VARCHAR(20) NULL,
    ADD COLUMN delete_reason VARCHAR(500) NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(id);