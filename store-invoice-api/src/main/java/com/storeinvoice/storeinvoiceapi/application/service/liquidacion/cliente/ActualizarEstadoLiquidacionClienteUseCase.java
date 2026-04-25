package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente;

import com.storeinvoice.storeinvoiceapi.application.dto.request.ActualizarEstadoLiquidacionClienteRequest;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActualizarEstadoLiquidacionClienteUseCase {

    private final LiquidacionRepository liquidacionRepository;

    @Transactional
    public LiquidacionCliente execute(final Long idLiquidacion, final ActualizarEstadoLiquidacionClienteRequest request) {
        final LiquidacionCliente liquidacion = liquidacionRepository.findClienteById(idLiquidacion)
                .orElseThrow(() -> new IllegalArgumentException("Liquidacion de cliente no encontrada"));
                
        liquidacion.setEstadoLiquidacion(request.estadoLiquidacion());
        
        return liquidacionRepository.saveCliente(liquidacion);
    }
}
