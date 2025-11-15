ALTER TABLE audit_log
    ALTER COLUMN old_value_json TYPE TEXT;

ALTER TABLE audit_log
    ALTER COLUMN new_value_json TYPE TEXT;
