package com.storeinvoice.storeinvoiceapi.application.dto.request;

import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;

import java.math.BigDecimal;

public record CrearLiquidacionClienteRequest(
                Long idPedido,
                Long idCliente,
                FormaPago formaPago,

                BigDecimal montoLiquidado) {
}
