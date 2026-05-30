-- Fix: Convert audit_logs.user_email from bytea to VARCHAR
-- Execute this script to fix: ERROR: no existe la función lower(bytea)

-- Back up data if needed (optional):
-- CREATE TABLE audit_logs_backup AS SELECT * FROM audit_logs;

-- Convert the column type from bytea to VARCHAR
ALTER TABLE IF EXISTS audit_logs
  ALTER COLUMN user_email TYPE VARCHAR(120) USING
    CASE
      WHEN user_email IS NULL THEN NULL
      ELSE convert_from(user_email, 'UTF8')
    END;
