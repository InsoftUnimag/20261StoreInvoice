package com.storeinvoice.store_invoice_api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "liquidaciones_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionClienteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_liquidacion")
    private Long idLiquidacion;

    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "forma_pago", nullable = false, length = 50)
    private String formaPago;

    @Column(name = "estado_liquidacion", nullable = false, length = 50)
    private String estadoLiquidacion;

    @Column(name = "fecha_liquidacion", nullable = false)
    private LocalDateTime fechaLiquidacion;

    @Column(name = "uri_pdf", length = 500)
    private String uriPdf;

    @Column(name = "monto_liquidado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoLiquidado;
}