-- Migration V2: Create task table
CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    due_date DATE,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(10) NOT NULL,
    user_id BIGINT,
    change_log TEXT,
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE SET NULL
);

CREATE INDEX idx_task_status ON task(status);
CREATE INDEX idx_task_priority ON task(priority);
CREATE INDEX idx_task_user_id ON task(user_id);
CREATE INDEX idx_task_due_date ON task(due_date); 