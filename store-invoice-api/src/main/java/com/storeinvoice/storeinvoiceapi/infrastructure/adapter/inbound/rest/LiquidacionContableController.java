package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionContadorResponse;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.contable.ConsultarLiquidacionContableUseCase;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionContable;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.LiquidacionContableMapper;
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
public class LiquidacionContableController {

    private final ConsultarLiquidacionContableUseCase consultarLiquidacionContableUseCase;
    private final LiquidacionContableMapper liquidacionContableMapper;

    @GetMapping("/liquidaciones")
    public ResponseEntity<List<LiquidacionContadorResponse>> consultarLiquidaciones(
            @RequestParam(required = false) final String tipo,
            @RequestParam(required = false) final Long idSujeto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") @Min(0) final int pagina,
            @RequestParam(defaultValue = "20") @Min(1) final int tamanoPagina) {

        final List<LiquidacionContable> liquidaciones =
                consultarLiquidacionContableUseCase.execute(tipo, idSujeto, fechaDesde, fechaHasta, pagina, tamanoPagina);

        final List<LiquidacionContadorResponse> response =
                liquidacionContableMapper.toResponseList(liquidaciones);

        return ResponseEntity.ok(response);
    }
}

