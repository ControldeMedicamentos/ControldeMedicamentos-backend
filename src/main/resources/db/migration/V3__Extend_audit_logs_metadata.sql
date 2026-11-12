ALTER TABLE IF EXISTS audit_logs
    ADD COLUMN IF NOT EXISTS ip_address VARCHAR(64),
    ADD COLUMN IF NOT EXISTS request_method VARCHAR(10),
    ADD COLUMN IF NOT EXISTS request_path VARCHAR(300),
    ADD COLUMN IF NOT EXISTS resultado VARCHAR(20),
    ADD COLUMN IF NOT EXISTS entidad_tipo VARCHAR(60),
    ADD COLUMN IF NOT EXISTS entidad_id VARCHAR(80),
    ADD COLUMN IF NOT EXISTS user_agent VARCHAR(500);

UPDATE audit_logs
SET resultado = 'EXITOSO'
WHERE resultado IS NULL;

ALTER TABLE IF EXISTS audit_logs
    ALTER COLUMN resultado SET DEFAULT 'EXITOSO',
    ALTER COLUMN resultado SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_audit_logs_ip_address ON audit_logs(ip_address);
CREATE INDEX IF NOT EXISTS idx_audit_logs_resultado ON audit_logs(resultado);
