CREATE TABLE IF NOT EXISTS conciliaciones_mensuales (
    id BIGSERIAL PRIMARY KEY,
    periodo VARCHAR(6) NOT NULL UNIQUE,
    resultado VARCHAR(25) NOT NULL,
    observacion TEXT,
    usuario_registro VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS conciliaciones_mensuales_detalle (
    id BIGSERIAL PRIMARY KEY,
    conciliacion_id BIGINT NOT NULL REFERENCES conciliaciones_mensuales(id) ON DELETE CASCADE,
    medicamento_id BIGINT NOT NULL REFERENCES medicamentos(id),
    stock_sistema INTEGER NOT NULL CHECK (stock_sistema >= 0),
    stock_fisico INTEGER NOT NULL CHECK (stock_fisico >= 0),
    diferencia INTEGER NOT NULL,
    CONSTRAINT uk_conciliacion_medicamento UNIQUE (conciliacion_id, medicamento_id)
);

CREATE INDEX IF NOT EXISTS idx_conciliacion_periodo ON conciliaciones_mensuales(periodo);
CREATE INDEX IF NOT EXISTS idx_conciliacion_detalle_medicamento
    ON conciliaciones_mensuales_detalle(medicamento_id);
