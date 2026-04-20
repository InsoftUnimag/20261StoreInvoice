package com.storeinvoice.store_invoice_api.infrastructure.adapter.inbound.rest;

import com.storeinvoice.store_invoice_api.application.dto.command.RegistrarFormaPagoCommand;
import com.storeinvoice.store_invoice_api.application.dto.response.FormaPagoResponse;
import com.storeinvoice.store_invoice_api.infrastructure.port.inbound.FormaPagoInboundPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/clientes")
public class FormaPagoController {

    private static final Logger LOG = LoggerFactory.getLogger(FormaPagoController.class);

    private final FormaPagoInboundPort formaPagoInboundPort;

    public FormaPagoController(FormaPagoInboundPort formaPagoInboundPort) {
        this.formaPagoInboundPort = formaPagoInboundPort;
    }

    @PostMapping("/ingresar-forma-pago")
    public Mono<ResponseEntity<FormaPagoResponse>> registrarFormaPago(
            @Valid @RequestBody RegistrarFormaPagoCommand command) {
        LOG.info("registrarFormaPago: {}", command);
        return formaPagoInboundPort.registrarFormaPago(command)
                .map(ResponseEntity::ok);
    }
}