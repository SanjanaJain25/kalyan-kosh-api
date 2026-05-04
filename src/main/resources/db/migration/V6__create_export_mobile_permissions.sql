CREATE TABLE IF NOT EXISTS export_mobile_permissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(20) NOT NULL,
    enabled BIT NOT NULL DEFAULT 0,
    granted_by_id VARCHAR(20) NULL,
    granted_at DATETIME(6) NULL,
    revoked_by_id VARCHAR(20) NULL,
    revoked_at DATETIME(6) NULL,
    remarks VARCHAR(1000) NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_export_mobile_permission_user UNIQUE (user_id),

    CONSTRAINT fk_export_mobile_permission_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_export_mobile_permission_granted_by
        FOREIGN KEY (granted_by_id) REFERENCES users(id),

    CONSTRAINT fk_export_mobile_permission_revoked_by
        FOREIGN KEY (revoked_by_id) REFERENCES users(id)
);

CREATE INDEX idx_export_mobile_permission_user
    ON export_mobile_permissions(user_id);

CREATE INDEX idx_export_mobile_permission_enabled
    ON export_mobile_permissions(enabled);