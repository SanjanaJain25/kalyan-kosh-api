CREATE TABLE IF NOT EXISTS manager_delete_permissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(20) NOT NULL,
    enabled BIT NOT NULL DEFAULT 0,
    granted_by_id VARCHAR(20) NULL,
    granted_at DATETIME(6) NULL,
    revoked_by_id VARCHAR(20) NULL,
    revoked_at DATETIME(6) NULL,
    remarks VARCHAR(1000) NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_manager_delete_permission_user UNIQUE (user_id),

    CONSTRAINT fk_manager_delete_permission_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_manager_delete_permission_granted_by
        FOREIGN KEY (granted_by_id) REFERENCES users(id),

    CONSTRAINT fk_manager_delete_permission_revoked_by
        FOREIGN KEY (revoked_by_id) REFERENCES users(id)
);

CREATE INDEX idx_manager_delete_permission_user
    ON manager_delete_permissions(user_id);

CREATE INDEX idx_manager_delete_permission_enabled
    ON manager_delete_permissions(enabled);