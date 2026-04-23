CREATE TABLE liquidaciones_cliente (
    id_liquidacion BIGSERIAL PRIMARY KEY,
    id_pedido BIGINT NOT NULL,
    id_cliente BIGINT NOT NULL,
    forma_pago VARCHAR(50) NOT NULL,
    estado_liquidacion VARCHAR(50) NOT NULL,
    fecha_liquidacion TIMESTAMP NOT NULL,
    uri_pdf VARCHAR(500),
    monto_liquidado DECIMAL(15, 2) NOT NULL
);

CREATE INDEX idx_liquidaciones_cliente_id_cliente ON liquidaciones_cliente(id_cliente);
CREATE INDEX idx_liquidaciones_cliente_fecha ON liquidaciones_cliente(fecha_liquidacion DESC);