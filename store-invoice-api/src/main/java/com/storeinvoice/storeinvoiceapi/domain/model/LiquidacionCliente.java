package com.storeinvoice.storeinvoiceapi.domain.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiquidacionCliente {

    private Long idLiquidacion;

    @NotNull(message = "ID de pedido es requerido")
    private Long idPedido;

    @NotNull(message = "ID de cliente es requerido")
    private Long idCliente;

    @NotNull(message = "Forma de pago es requerida")
    private FormaPago formaPago;

    @NotNull(message = "Estado de liquidacion es requerido")
    private EstadoLiquidacion estadoLiquidacion;

    @NotNull(message = "Fecha de liquidacion es requerida")
    private LocalDateTime fechaLiquidacion;

    private String uriPdf;

    @NotNull(message = "Monto liquidado es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monto debe ser mayor a cero")
    private BigDecimal montoLiquidado;
}