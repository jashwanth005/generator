-- Create test execution results table
CREATE TABLE IF NOT EXISTS test_execution_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id VARCHAR(255) NOT NULL,
    test_case_id VARCHAR(255) NOT NULL,
    execution_status VARCHAR(255) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    execution_duration_ms BIGINT,
    error_message VARCHAR(1000)
);

-- Create test steps table
CREATE TABLE IF NOT EXISTS test_step (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_execution_result_id BIGINT NOT NULL,
    step_number INT NOT NULL,
    description VARCHAR(1000),
    expected_result VARCHAR(1000),
    actual_result VARCHAR(1000),
    status VARCHAR(255) NOT NULL,
    error_message VARCHAR(1000),
    screenshot_path VARCHAR(1000),
    execution_duration_ms BIGINT,
    FOREIGN KEY (test_execution_result_id) REFERENCES test_execution_results(id)
);

-- Create automation scripts table
CREATE TABLE IF NOT EXISTS automation_scripts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id VARCHAR(255) NOT NULL,
    test_case_id VARCHAR(255) NOT NULL,
    script_content LONGTEXT,
    script_language VARCHAR(255) NOT NULL,
    script_type VARCHAR(255) NOT NULL,
    creation_date DATETIME NOT NULL,
    file_path VARCHAR(1000),
    status VARCHAR(255),
    base_url VARCHAR(1000),
    test_scenario TEXT
);

-- Create test cases table
CREATE TABLE IF NOT EXISTS test_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    creation_date DATETIME NOT NULL,
    title VARCHAR(255),
    description TEXT
); 