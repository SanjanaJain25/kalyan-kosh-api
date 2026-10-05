CREATE TABLE IF NOT EXISTS blog_posts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    image_base64 LONGTEXT NOT NULL,
    image_content_type VARCHAR(100) NOT NULL,
    image_file_name VARCHAR(255) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_blog_posts_active_created (is_active, created_at)
);
