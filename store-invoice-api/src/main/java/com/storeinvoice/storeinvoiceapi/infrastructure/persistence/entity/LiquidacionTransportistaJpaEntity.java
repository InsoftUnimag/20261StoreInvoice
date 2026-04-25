package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "liquidaciones_transportista")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionTransportistaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_liquidacion")
    private Long idLiquidacion;

    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Column(name = "id_transportista", nullable = false)
    private Long idTransportista;

    @Column(name = "monto_calculado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoCalculado;

    @Column(name = "fecha_liquidacion", nullable = false)
    private LocalDateTime fechaLiquidacion;
}
