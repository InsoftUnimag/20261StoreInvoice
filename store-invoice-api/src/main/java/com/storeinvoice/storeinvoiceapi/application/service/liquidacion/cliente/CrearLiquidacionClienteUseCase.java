package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente;

import com.storeinvoice.storeinvoiceapi.application.dto.request.CrearLiquidacionClienteRequest;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrearLiquidacionClienteUseCase {

    private final LiquidacionRepository liquidacionRepository;

    @Transactional
    public LiquidacionCliente execute(final CrearLiquidacionClienteRequest request) {
        final LiquidacionCliente liquidacion = new LiquidacionCliente();
        liquidacion.setIdPedido(request.idPedido());
        liquidacion.setIdCliente(request.idCliente());
        liquidacion.setFormaPago(request.formaPago());
        liquidacion.setMontoLiquidado(request.montoLiquidado());
        
        liquidacion.setEstadoLiquidacion(EstadoLiquidacion.PENDIENTE);
        liquidacion.setFechaLiquidacion(LocalDateTime.now());
        
        return liquidacionRepository.saveCliente(liquidacion);
    }
}

