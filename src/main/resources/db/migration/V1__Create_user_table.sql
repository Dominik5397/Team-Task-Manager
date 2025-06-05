-- Migration V1: Create user table
CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    avatar_url VARCHAR(500)
);

CREATE INDEX idx_user_email ON app_user(email);
CREATE INDEX idx_user_username ON app_user(username); 