package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.command.FormaPagoCommand;
import com.storeinvoice.storeinvoiceapi.application.dto.command.RegistrarFormaPagoClienteCommand;
import com.storeinvoice.storeinvoiceapi.application.dto.response.FormaPagoClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.dto.response.TieneFormaPagoResponse;
import com.storeinvoice.storeinvoiceapi.application.service.formapago.ActualizarFormaPagoClienteUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.formapago.ConsultarFormaPagoLiquidacionUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.formapago.ConsultarFormaPagoPorClienteUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.formapago.RegistrarFormaPagoClienteUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.formapago.VerificarTieneFormaPagoLiquidacionUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.formapago.VerificarTieneFormaPagoPorClienteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Forma de Pago Cliente", description = "Operaciones relacionadas con la forma de pago de los clientes")
@RequiredArgsConstructor
public class FormaPagoClienteController {

    private final ConsultarFormaPagoLiquidacionUseCase consultarFormaPagoLiquidacionUseCase;
    private final ConsultarFormaPagoPorClienteUseCase consultarFormaPagoPorClienteUseCase;
    private final VerificarTieneFormaPagoLiquidacionUseCase verificarTieneFormaPagoLiquidacionUseCase;
    private final VerificarTieneFormaPagoPorClienteUseCase verificarTieneFormaPagoPorClienteUseCase;
    private final RegistrarFormaPagoClienteUseCase registrarFormaPagoClienteUseCase;
    private final ActualizarFormaPagoClienteUseCase actualizarFormaPagoClienteUseCase;


    @GetMapping("/pedidos/{id_pedido}/forma-pago")
    @Operation(summary = "Consultar forma de pago de un cliente por ID de pedido (desde liquidacion)")
    public Mono<ResponseEntity<FormaPagoClienteResponse>> consultarFormaPagoPorPedido(
            @Parameter(description = "ID del pedido", required = true, example = "1")
            @PathVariable @NotNull Long id_pedido) {
        return consultarFormaPagoLiquidacionUseCase.ejecutar(id_pedido)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/pedidos/{id_pedido}/tiene-forma-pago")
    @Operation(summary = "Verificar si el cliente de un pedido tiene forma de pago registrada (desde liquidacion)")
    public Mono<ResponseEntity<TieneFormaPagoResponse>> tieneFormaPagoPorPedido(
            @Parameter(description = "ID del pedido", required = true, example = "1")
            @PathVariable @NotNull Long id_pedido) {
        return verificarTieneFormaPagoLiquidacionUseCase.ejecutar(id_pedido)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/clientes/{id_cliente}/forma-pago")
    @Operation(summary = "Consultar forma de pago actual de un cliente por su ID")
    public Mono<ResponseEntity<FormaPagoClienteResponse>> consultarFormaPago(
            @Parameter(description = "ID del cliente", required = true, example = "1")
            @PathVariable @NotNull Long id_cliente) {
        return consultarFormaPagoPorClienteUseCase.ejecutar(id_cliente)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/clientes/{id_cliente}/tiene-forma-pago")
    @Operation(summary = "Verificar si un cliente tiene forma de pago registrada")
    public ResponseEntity<TieneFormaPagoResponse> tieneFormaPago(
            @Parameter(description = "ID del cliente", required = true, example = "1")
            @PathVariable @NotNull Long id_cliente) {
        return ResponseEntity.ok(verificarTieneFormaPagoPorClienteUseCase.ejecutar(id_cliente));
    }

    @PostMapping("/clientes/{id_cliente}/forma-pago")
    @Operation(summary = "Registrar forma de pago de un cliente")
    public Mono<ResponseEntity<FormaPagoClienteResponse>> registrarFormaPago(
            @Parameter(description = "ID del cliente", required = true, example = "1")
            @PathVariable @NotNull Long id_cliente,
            @RequestBody @Valid FormaPagoCommand command) {
        return registrarFormaPagoClienteUseCase.ejecutar(id_cliente, command.formaPago())
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    @PutMapping("/clientes/{id_cliente}/actualizar-forma-pago")
    @Operation(summary = "Actualizar forma de pago de un cliente")
    public Mono<ResponseEntity<FormaPagoClienteResponse>> actualizarFormaPago(
            @Parameter(description = "ID del cliente", required = true, example = "1")
            @PathVariable @NotNull Long id_cliente,
            @RequestBody @Valid FormaPagoCommand command) {
        return actualizarFormaPagoClienteUseCase.ejecutar(id_cliente, command.formaPago())
                .map(ResponseEntity::ok);
    }
}

