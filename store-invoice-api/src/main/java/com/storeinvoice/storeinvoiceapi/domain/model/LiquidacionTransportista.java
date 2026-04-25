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
public class LiquidacionTransportista {

    private Long idLiquidacion;

    @NotNull(message = "ID de pedido es requerido")
    private Long idPedido;

    @NotNull(message = "ID de transportista es requerido")
    private Long idTransportista;

    @NotNull(message = "Monto calculado es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monto debe ser mayor a cero")
    private BigDecimal montoCalculado;

    @NotNull(message = "Fecha de liquidacion es requerida")
    private LocalDateTime fechaLiquidacion;
}
