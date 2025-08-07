ALTER TABLE test_execution_results
    MODIFY COLUMN error_message TEXT;

ALTER TABLE test_step
    MODIFY COLUMN error_message TEXT; 