CREATE TABLE IF NOT EXISTS pedidos (
    id_pedido BIGINT PRIMARY KEY,
    id_cliente BIGINT NOT NULL,
    precio_total DECIMAL(15, 2) NOT NULL,
    fecha_recibido TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pedidos_id_cliente ON pedidos(id_cliente);

-- Tabla de seguimiento de eventos recibidos del Módulo de Transporte
CREATE TABLE IF NOT EXISTS evento_recibido (
    id_pedido BIGINT PRIMARY KEY,
    tasa_efectividad INT NOT NULL,
    id_transportista BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_recibido TIMESTAMP NOT NULL,
    fecha_procesado TIMESTAMP
);
