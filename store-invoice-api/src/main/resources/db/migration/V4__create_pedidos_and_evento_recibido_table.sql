-- Tabla de seguimiento de eventos recibidos del Modulo de Transporte
CREATE TABLE IF NOT EXISTS evento_recibido (
    id_pedido BIGINT PRIMARY KEY,
    tasa_efectividad INT NOT NULL,
    id_transportista BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_recibido TIMESTAMP NOT NULL,
    fecha_procesado TIMESTAMP
);
