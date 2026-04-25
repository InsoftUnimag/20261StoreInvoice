package com.storeinvoice.storeinvoiceapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo de dominio que representa la liquidación generada para un cliente.
 * Contiene el monto a cobrar/devolver al cliente según la forma de pago y
 * el estado de la entrega.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionCliente {

    private Long idLiquidacion;
    private Long idPedido;
    private Long idCliente;
    private FormaPago formaPago;
    private EstadoLiquidacion estadoLiquidacion;
    private LocalDateTime fechaLiquidacion;
    private String uriPdf;
    private BigDecimal montoLiquidado;
}