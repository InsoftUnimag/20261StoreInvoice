package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoJpaEntity {

    @Id
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "precio_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioTotal;

    @Column(name = "fecha_recibido", nullable = false)
    private LocalDateTime fechaRecibido;
}

