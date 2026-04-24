CREATE TABLE forma_pago_cliente (
    id_cliente BIGINT PRIMARY KEY,
    forma_pago VARCHAR(50) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL,
    CONSTRAINT chk_forma_pago CHECK (forma_pago IN ('CONTRA_ENTREGA', 'CARTERA_COMERCIAL'))
);

CREATE INDEX idx_forma_pago_cliente_forma_pago ON forma_pago_cliente(forma_pago);
