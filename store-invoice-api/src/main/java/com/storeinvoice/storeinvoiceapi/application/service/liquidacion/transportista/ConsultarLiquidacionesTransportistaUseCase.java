package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista;

import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultarLiquidacionesTransportistaUseCase {

    private final LiquidacionRepository liquidacionRepository;

    public List<LiquidacionTransportista> execute(final Long idTransportista, final int pagina, final int tamanoPagina) {
        return liquidacionRepository.findByIdTransportista(idTransportista, pagina, tamanoPagina);
    }
}


