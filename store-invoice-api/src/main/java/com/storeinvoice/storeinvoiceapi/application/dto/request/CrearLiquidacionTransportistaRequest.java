package com.storeinvoice.storeinvoiceapi.application.dto.request;

import java.math.BigDecimal;

public record CrearLiquidacionTransportistaRequest(
                Long idPedido,

                Long idTransportista,

                BigDecimal montoCalculado) {
}
