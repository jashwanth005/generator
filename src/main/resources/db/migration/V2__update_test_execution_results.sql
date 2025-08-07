-- Rename execution time columns
ALTER TABLE test_execution_results 
    CHANGE COLUMN execution_start_time start_time DATETIME NOT NULL,
    CHANGE COLUMN execution_end_time end_time DATETIME NULL;

-- Drop unused columns
ALTER TABLE test_execution_results 
    DROP COLUMN IF EXISTS browser_type,
    DROP COLUMN IF EXISTS browser_version,
    DROP COLUMN IF EXISTS test_environment,
    DROP COLUMN IF EXISTS report_path,
    DROP COLUMN IF EXISTS stack_trace,
    DROP COLUMN IF EXISTS automation_script_id;

-- Add missing columns if they don't exist
ALTER TABLE test_execution_results 
    ADD COLUMN IF NOT EXISTS execution_duration_ms BIGINT NULL AFTER end_time,
    MODIFY COLUMN error_message VARCHAR(1000) NULL; 