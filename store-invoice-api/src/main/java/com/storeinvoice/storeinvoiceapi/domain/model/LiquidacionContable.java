package com.storeinvoice.storeinvoiceapi.domain.model;

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

    private Long idPedido;

    private String tipoLiquidacion;

    private Long idSujeto;

    private BigDecimal monto;

    private LocalDateTime fechaLiquidacion;

    private String uriDocumento;
}
