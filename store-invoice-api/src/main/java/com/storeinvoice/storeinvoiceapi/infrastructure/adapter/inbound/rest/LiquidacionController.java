package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.ConsultarLiquidacionesClienteUseCase;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.LiquidacionEntityMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clientes")
@Validated
public class LiquidacionController {

    private static final int TAMANO_PAGINA_POR_DEFECTO = 20;

    private final ConsultarLiquidacionesClienteUseCase consultarLiquidacionesUseCase;
    private final LiquidacionEntityMapper liquidacionMapper;

    public LiquidacionController(
            final ConsultarLiquidacionesClienteUseCase consultarLiquidacionesUseCase,
            final LiquidacionEntityMapper liquidacionMapper) {
        this.consultarLiquidacionesUseCase = consultarLiquidacionesUseCase;
        this.liquidacionMapper = liquidacionMapper;
    }

    @GetMapping("/{idCliente}/liquidaciones")
    public ResponseEntity<List<LiquidacionClienteResponse>> consultarLiquidaciones(
            @PathVariable @NotNull @Min(1) final Long idCliente,
            @RequestParam(defaultValue = "0") @Min(0) final int pagina,
            @RequestParam(defaultValue = "20") @Min(1) final int tamanoPagina) {
        
        final int tamano = tamanoPagina > 0 ? tamanoPagina : TAMANO_PAGINA_POR_DEFECTO;
        final List<LiquidacionCliente> liquidaciones = 
                consultarLiquidacionesUseCase.execute(idCliente, pagina, tamano);
        
        final List<LiquidacionClienteResponse> response = 
                liquidacionMapper.toResponseList(liquidaciones);
        
        return ResponseEntity.ok(response);
    }
}