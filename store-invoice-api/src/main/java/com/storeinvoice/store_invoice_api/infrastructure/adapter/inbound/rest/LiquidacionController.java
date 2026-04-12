package com.storeinvoice.store_invoice_api.infrastructure.adapter.inbound.rest;

import com.storeinvoice.store_invoice_api.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.store_invoice_api.application.service.liquidacion.ConsultarLiquidacionesClienteUseCase;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(LiquidacionController.class);
    private static final int TAMAÑO_PÁGINA_POR_DEFECTO = 20;

    private final ConsultarLiquidacionesClienteUseCase consultarLiquidacionesUseCase;

    public LiquidacionController(final ConsultarLiquidacionesClienteUseCase consultarLiquidacionesUseCase) {
        this.consultarLiquidacionesUseCase = consultarLiquidacionesUseCase;
    }

    @GetMapping("/{idCliente}/liquidaciones")
    public ResponseEntity<List<LiquidacionClienteResponse>> consultarLiquidaciones(
            @PathVariable @NotNull @Min(1) final Long idCliente,
            @RequestParam(defaultValue = "0") @Min(0) final int pagina,
            @RequestParam(defaultValue = "20") @Min(1) final int tamañoPagina) {
        
        LOG.info("Consultando liquidaciones para cliente: {}, página: {}, tamaño: {}", 
                idCliente, pagina, tamañoPagina);
        
        final int tamaño = tamañoPagina > 0 ? tamañoPagina : TAMAÑO_PÁGINA_POR_DEFECTO;
        final List<LiquidacionClienteResponse> liquidaciones = 
                consultarLiquidacionesUseCase.execute(idCliente, pagina, tamaño);
        
        LOG.info("Se encontraron {} liquidaciones para cliente: {}", liquidaciones.size(), idCliente);
        return ResponseEntity.ok(liquidaciones);
    }
}