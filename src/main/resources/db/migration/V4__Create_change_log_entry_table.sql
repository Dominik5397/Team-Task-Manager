-- Migration V4: Create change_log_entry table for detailed change tracking
CREATE TABLE change_log_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    old_value VARCHAR(1000),
    new_value VARCHAR(1000),
    operation_type VARCHAR(20) NOT NULL,
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by_user_id) REFERENCES app_user(id) ON DELETE SET NULL
);

-- Indeksy dla optymalizacji zapytań
CREATE INDEX idx_change_log_task_id ON change_log_entry(task_id);
CREATE INDEX idx_change_log_changed_at ON change_log_entry(changed_at);
CREATE INDEX idx_change_log_operation_type ON change_log_entry(operation_type);
CREATE INDEX idx_change_log_field_name ON change_log_entry(field_name);
CREATE INDEX idx_change_log_changed_by ON change_log_entry(changed_by_user_id);
CREATE INDEX idx_change_log_task_changed_at ON change_log_entry(task_id, changed_at); 