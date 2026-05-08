package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionContadorResponse;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.contable.ConsultarLiquidacionContableUseCase;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionContable;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.LiquidacionContableMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
@Tag(name = "Liquidaciones Contables", description = "Operaciones relacionadas con liquidaciones contables")
public class LiquidacionContableController {

    private final ConsultarLiquidacionContableUseCase consultarLiquidacionContableUseCase;
    private final LiquidacionContableMapper liquidacionContableMapper;

    @GetMapping("/liquidaciones")
    @Operation(summary = "Consultar liquidaciones contables", description = "Obtiene liquidaciones contables con filtros opcionales y paginación")
    public ResponseEntity<List<LiquidacionContadorResponse>> consultarLiquidaciones(
            @Parameter(description = "Tipo de sujeto (CLIENTE, TRANSPORTISTA)")
            @RequestParam(required = false) final String tipo,
            @Parameter(description = "ID del sujeto")
            @RequestParam(required = false) final Long idSujeto,
            @Parameter(description = "Fecha de inicio del rango (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate fechaDesde,
            @Parameter(description = "Fecha fin del rango (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate fechaHasta,
            @Parameter(description = "Número de página (desde 0)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) final int pagina,
            @Parameter(description = "Tamaño de la página", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) final int tamanoPagina) {

        final List<LiquidacionContable> liquidaciones =
                consultarLiquidacionContableUseCase.execute(tipo, idSujeto, fechaDesde, fechaHasta, pagina, tamanoPagina);

        final List<LiquidacionContadorResponse> response =
                liquidacionContableMapper.toResponseList(liquidaciones);

        return ResponseEntity.ok(response);
    }
}

