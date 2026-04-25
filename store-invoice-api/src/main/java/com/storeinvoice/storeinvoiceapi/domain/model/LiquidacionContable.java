package com.storeinvoice.storeinvoiceapi.domain.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class LiquidacionContable {

    private Long idLiquidacion;

    @NotNull(message = "ID de pedido es requerido")
    private Long idPedido;

    @NotBlank(message = "Tipo de liquidacion es requerido")
    private String tipoLiquidacion;

    @NotNull(message = "ID de sujeto es requerido")
    private Long idSujeto;

    @NotNull(message = "Monto es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monto debe ser mayor a cero")
    private BigDecimal monto;

    @NotNull(message = "Fecha de liquidacion es requerida")
    private LocalDateTime fechaLiquidacion;

    private String uriDocumento;
}
