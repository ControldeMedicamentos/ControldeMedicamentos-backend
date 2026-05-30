-- Fix: Convert audit_logs.user_email from bytea to VARCHAR
-- This resolves the error: "no existe la función lower(bytea)"

-- Check if the column exists and convert it if needed
ALTER TABLE audit_logs
ALTER COLUMN user_email TYPE VARCHAR(120) USING user_email::text;
