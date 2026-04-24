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
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.LiquidacionEntityMapper;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.LiquidacionTransportistaMapper;
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

@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
public class LiquidacionController {

    private final ConsultarLiquidacionesClienteUseCase consultarLiquidacionesUseCase;
    private final ConsultarLiquidacionesTransportistaUseCase consultarLiquidacionesTransportistaUseCase;
    private final CrearLiquidacionClienteUseCase crearLiquidacionClienteUseCase;
    private final ActualizarEstadoLiquidacionClienteUseCase actualizarEstadoLiquidacionClienteUseCase;
    private final CrearLiquidacionTransportistaUseCase crearLiquidacionTransportistaUseCase;
    private final ActualizarMontoLiquidacionTransportistaUseCase actualizarMontoLiquidacionTransportistaUseCase;
    private final LiquidacionEntityMapper liquidacionMapper;
    private final LiquidacionTransportistaMapper transportistaMapper;

    @GetMapping("/clientes/{idCliente}/liquidaciones")
    public ResponseEntity<List<LiquidacionClienteResponse>> consultarLiquidaciones(
            @PathVariable @NotNull @Min(1) final Long idCliente,
            @RequestParam(defaultValue = "0") @Min(0) final int pagina,
            @RequestParam(defaultValue = "20") @Min(1) final int tamanoPagina) {

        final List<LiquidacionCliente> liquidaciones =
                consultarLiquidacionesUseCase.execute(idCliente, pagina, tamanoPagina);

        final List<LiquidacionClienteResponse> response =
                liquidacionMapper.toResponseList(liquidaciones);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/transportistas/{idTransportista}/liquidaciones")
    public ResponseEntity<List<LiquidacionTransportistaResponse>> consultarLiquidacionesTransportista(
            @PathVariable @NotNull @Min(1) final Long idTransportista,
            @RequestParam(defaultValue = "0") @Min(0) final int pagina,
            @RequestParam(defaultValue = "20") @Min(1) final int tamanoPagina) {

        final List<LiquidacionTransportista> liquidaciones =
                consultarLiquidacionesTransportistaUseCase.execute(idTransportista, pagina, tamanoPagina);

        final List<LiquidacionTransportistaResponse> response =
                transportistaMapper.toResponseList(liquidaciones);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/clientes/liquidaciones")
    public ResponseEntity<LiquidacionClienteResponse> crearLiquidacionCliente(
            @RequestBody @Validated final CrearLiquidacionClienteRequest request) {

        final LiquidacionCliente liquidacion = crearLiquidacionClienteUseCase.execute(request);
        final LiquidacionClienteResponse response = liquidacionMapper.toResponse(liquidacion);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/clientes/liquidaciones/{idLiquidacion}/estado")
    public ResponseEntity<LiquidacionClienteResponse> actualizarEstadoLiquidacionCliente(
            @PathVariable final Long idLiquidacion,
            @RequestBody @Validated final ActualizarEstadoLiquidacionClienteRequest request) {

        final LiquidacionCliente liquidacion = actualizarEstadoLiquidacionClienteUseCase.execute(idLiquidacion, request);
        final LiquidacionClienteResponse response = liquidacionMapper.toResponse(liquidacion);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/transportistas/liquidaciones")
    public ResponseEntity<LiquidacionTransportistaResponse> crearLiquidacionTransportista(
            @RequestBody @Validated final CrearLiquidacionTransportistaRequest request) {

        final LiquidacionTransportista liquidacion = crearLiquidacionTransportistaUseCase.execute(request);
        final LiquidacionTransportistaResponse response = transportistaMapper.toResponse(liquidacion);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/transportistas/liquidaciones/{idLiquidacion}/monto")
    public ResponseEntity<LiquidacionTransportistaResponse> actualizarMontoLiquidacionTransportista(
            @PathVariable final Long idLiquidacion,
            @RequestBody @Validated final ActualizarMontoLiquidacionTransportistaRequest request) {

        final LiquidacionTransportista liquidacion = actualizarMontoLiquidacionTransportistaUseCase.execute(idLiquidacion, request);
        final LiquidacionTransportistaResponse response = transportistaMapper.toResponse(liquidacion);

        return ResponseEntity.ok(response);
    }
}

