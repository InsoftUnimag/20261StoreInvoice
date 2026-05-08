package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.request.ActualizarEstadoLiquidacionClienteRequest;
import com.storeinvoice.storeinvoiceapi.application.dto.request.ActualizarMontoLiquidacionTransportistaRequest;
import com.storeinvoice.storeinvoiceapi.application.dto.request.CrearLiquidacionClienteRequest;
import com.storeinvoice.storeinvoiceapi.application.dto.request.CrearLiquidacionTransportistaRequest;
import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionTransportistaResponse;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente.ActualizarEstadoLiquidacionClienteUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente.ConsultarLiquidacionesClienteUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente.CrearLiquidacionClienteUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista.ActualizarMontoLiquidacionTransportistaUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista.ConsultarLiquidacionesTransportistaUseCase;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista.CrearLiquidacionTransportistaUseCase;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest.mapper.LiquidacionClienteResponseMapper;
import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest.mapper.LiquidacionTransportistaResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones de liquidaciones de clientes y transportistas.
 * Usa los Response Mappers de la capa REST para convertir Domain Models a DTOs de respuesta.
 */
@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
@Tag(name = "Liquidaciones", description = "Operaciones relacionadas con liquidaciones de clientes y transportistas")
public class LiquidacionController {

    private final ConsultarLiquidacionesClienteUseCase consultarLiquidacionesUseCase;
    private final ConsultarLiquidacionesTransportistaUseCase consultarLiquidacionesTransportistaUseCase;
    private final CrearLiquidacionClienteUseCase crearLiquidacionClienteUseCase;
    private final ActualizarEstadoLiquidacionClienteUseCase actualizarEstadoLiquidacionClienteUseCase;
    private final CrearLiquidacionTransportistaUseCase crearLiquidacionTransportistaUseCase;
    private final ActualizarMontoLiquidacionTransportistaUseCase actualizarMontoLiquidacionTransportistaUseCase;
    private final LiquidacionClienteResponseMapper liquidacionClienteMapper;
    private final LiquidacionTransportistaResponseMapper liquidacionTransportistaMapper;

    @GetMapping("/clientes/{idCliente}/liquidaciones")
    @Operation(summary = "Consultar liquidaciones de un cliente", description = "Obtiene la lista de liquidaciones de un cliente con paginación")
    public ResponseEntity<List<LiquidacionClienteResponse>> consultarLiquidaciones(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable @NotNull @Min(1) final Long idCliente,
            @Parameter(description = "Número de página (desde 0)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) final int pagina,
            @Parameter(description = "Tamaño de la página", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) final int tamanoPagina) {

        final List<LiquidacionCliente> liquidaciones =
                consultarLiquidacionesUseCase.execute(idCliente, pagina, tamanoPagina);

        return ResponseEntity.ok(liquidacionClienteMapper.toResponseList(liquidaciones));
    }

    @GetMapping("/transportistas/{idTransportista}/liquidaciones")
    @Operation(summary = "Consultar liquidaciones de un transportista", description = "Obtiene la lista de liquidaciones de un transportista con paginación")
    public ResponseEntity<List<LiquidacionTransportistaResponse>> consultarLiquidacionesTransportista(
            @Parameter(description = "ID del transportista", required = true)
            @PathVariable @NotNull @Min(1) final Long idTransportista,
            @Parameter(description = "Número de página (desde 0)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) final int pagina,
            @Parameter(description = "Tamaño de la página", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) final int tamanoPagina) {

        final List<LiquidacionTransportista> liquidaciones =
                consultarLiquidacionesTransportistaUseCase.execute(idTransportista, pagina, tamanoPagina);

        return ResponseEntity.ok(liquidacionTransportistaMapper.toResponseList(liquidaciones));
    }

    @PostMapping("/clientes/liquidaciones")
    @Operation(summary = "Crear liquidación de cliente", description = "Crea una nueva liquidación para un cliente")
    public ResponseEntity<LiquidacionClienteResponse> crearLiquidacionCliente(
            @RequestBody @Validated final CrearLiquidacionClienteRequest request) {

        final LiquidacionCliente liquidacion = crearLiquidacionClienteUseCase.execute(request);
        return ResponseEntity.ok(liquidacionClienteMapper.toResponse(liquidacion));
    }

    @PutMapping("/clientes/liquidaciones/{idLiquidacion}/estado")
    @Operation(summary = "Actualizar estado de liquidación de cliente", description = "Actualiza el estado de una liquidación de cliente existente")
    public ResponseEntity<LiquidacionClienteResponse> actualizarEstadoLiquidacionCliente(
            @Parameter(description = "ID de la liquidación", required = true)
            @PathVariable final Long idLiquidacion,
            @RequestBody @Validated final ActualizarEstadoLiquidacionClienteRequest request) {

        final LiquidacionCliente liquidacion =
                actualizarEstadoLiquidacionClienteUseCase.execute(idLiquidacion, request);
        return ResponseEntity.ok(liquidacionClienteMapper.toResponse(liquidacion));
    }

    @PostMapping("/transportistas/liquidaciones")
    @Operation(summary = "Crear liquidación de transportista", description = "Crea una nueva liquidación para un transportista")
    public ResponseEntity<LiquidacionTransportistaResponse> crearLiquidacionTransportista(
            @RequestBody @Validated final CrearLiquidacionTransportistaRequest request) {

        final LiquidacionTransportista liquidacion = crearLiquidacionTransportistaUseCase.execute(request);
        return ResponseEntity.ok(liquidacionTransportistaMapper.toResponse(liquidacion));
    }

    @PutMapping("/transportistas/liquidaciones/{idLiquidacion}/monto")
    @Operation(summary = "Actualizar monto de liquidación de transportista", description = "Actualiza el monto de una liquidación de transportista existente")
    public ResponseEntity<LiquidacionTransportistaResponse> actualizarMontoLiquidacionTransportista(
            @Parameter(description = "ID de la liquidación", required = true)
            @PathVariable final Long idLiquidacion,
            @RequestBody @Validated final ActualizarMontoLiquidacionTransportistaRequest request) {

        final LiquidacionTransportista liquidacion =
                actualizarMontoLiquidacionTransportistaUseCase.execute(idLiquidacion, request);
        return ResponseEntity.ok(liquidacionTransportistaMapper.toResponse(liquidacion));
    }
}
