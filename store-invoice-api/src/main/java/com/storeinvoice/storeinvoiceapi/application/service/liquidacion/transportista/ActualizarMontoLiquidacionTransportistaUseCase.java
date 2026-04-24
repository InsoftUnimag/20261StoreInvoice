package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista;

import com.storeinvoice.storeinvoiceapi.application.dto.request.ActualizarMontoLiquidacionTransportistaRequest;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActualizarMontoLiquidacionTransportistaUseCase {

    private final LiquidacionRepository liquidacionRepository;

    @Transactional
    public LiquidacionTransportista execute(final Long idLiquidacion, final ActualizarMontoLiquidacionTransportistaRequest request) {
        final LiquidacionTransportista liquidacion = liquidacionRepository.findTransportistaById(idLiquidacion)
                .orElseThrow(() -> new IllegalArgumentException("Liquidacion de transportista no encontrada"));
                
        liquidacion.setMontoCalculado(request.montoCalculado());
        
        return liquidacionRepository.saveTransportista(liquidacion);
    }
}
