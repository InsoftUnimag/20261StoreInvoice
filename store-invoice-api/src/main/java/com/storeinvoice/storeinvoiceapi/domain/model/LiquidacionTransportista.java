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
public class LiquidacionTransportista {

    private Long idLiquidacion;

    private Long idPedido;

    private Long idTransportista;

    private BigDecimal montoCalculado;

    private LocalDateTime fechaLiquidacion;
}
