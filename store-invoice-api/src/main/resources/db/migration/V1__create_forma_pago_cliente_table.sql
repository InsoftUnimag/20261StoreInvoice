-- V2: Create forma_pago_cliente table
-- Stores the payment method associated with each client (id comes from external system)

CREATE TABLE IF NOT EXISTS formas_pago_cliente (
    id_cliente BIGINT PRIMARY KEY,
    forma_pago VARCHAR(50) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_forma_pago CHECK (forma_pago IN ('CONTRA_ENTREGA', 'CARTERA_COMERCIAL'))
);

CREATE INDEX idx_forma_pago_cliente_id_cliente ON formas_pago_cliente(id_cliente);