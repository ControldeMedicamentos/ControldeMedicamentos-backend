CREATE TABLE IF NOT EXISTS movimientos_inventario_lotes (
    id BIGSERIAL PRIMARY KEY,
    movimiento_id BIGINT NOT NULL REFERENCES movimientos_inventario(id),
    inventario_id BIGINT NOT NULL REFERENCES inventarios(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    stock_antes INTEGER NOT NULL CHECK (stock_antes >= 0),
    stock_despues INTEGER NOT NULL CHECK (stock_despues >= 0),
    CONSTRAINT uk_movimiento_inventario_lote UNIQUE (movimiento_id, inventario_id)
);

CREATE INDEX IF NOT EXISTS idx_movimiento_lote_movimiento
    ON movimientos_inventario_lotes(movimiento_id);
CREATE INDEX IF NOT EXISTS idx_movimiento_lote_inventario
    ON movimientos_inventario_lotes(inventario_id);
