package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista;

import com.storeinvoice.storeinvoiceapi.application.dto.request.CrearLiquidacionTransportistaRequest;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrearLiquidacionTransportistaUseCase {

    private final LiquidacionRepository liquidacionRepository;

    @Transactional
    public LiquidacionTransportista execute(final CrearLiquidacionTransportistaRequest request) {
        final LiquidacionTransportista liquidacion = new LiquidacionTransportista();
        liquidacion.setIdPedido(request.idPedido());
        liquidacion.setIdTransportista(request.idTransportista());
        liquidacion.setMontoCalculado(request.montoCalculado());
        
        liquidacion.setFechaLiquidacion(LocalDateTime.now());
        
        return liquidacionRepository.saveTransportista(liquidacion);
    }
}

