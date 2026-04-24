CREATE TABLE liquidaciones_transportista (
    id_liquidacion BIGSERIAL PRIMARY KEY,
    id_pedido BIGINT NOT NULL,
    id_transportista BIGINT NOT NULL,
    monto_calculado DECIMAL(15, 2) NOT NULL,
    fecha_liquidacion TIMESTAMP NOT NULL
);

CREATE INDEX idx_liquidaciones_transportista_id_transportista ON liquidaciones_transportista(id_transportista);
CREATE INDEX idx_liquidaciones_transportista_fecha ON liquidaciones_transportista(fecha_liquidacion DESC);
