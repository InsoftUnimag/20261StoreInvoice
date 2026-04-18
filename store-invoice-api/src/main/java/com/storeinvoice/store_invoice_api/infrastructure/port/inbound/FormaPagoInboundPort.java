package com.storeinvoice.store_invoice_api.infrastructure.port.inbound;

import com.storeinvoice.store_invoice_api.application.dto.command.RegistrarFormaPagoCommand;
import com.storeinvoice.store_invoice_api.application.dto.response.FormaPagoResponse;
import reactor.core.publisher.Mono;

public interface FormaPagoInboundPort {

    Mono<FormaPagoResponse> registrarFormaPago(RegistrarFormaPagoCommand command);
}