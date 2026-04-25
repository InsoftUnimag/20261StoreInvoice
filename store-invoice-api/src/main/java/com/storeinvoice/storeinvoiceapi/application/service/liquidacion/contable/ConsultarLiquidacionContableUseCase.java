package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.contable;

import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesContadorQuery;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionContableRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionContable;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultarLiquidacionContableUseCase {

    private final LiquidacionContableRepository liquidacionContableRepository;

    public List<LiquidacionContable> execute(
            final String tipo,
            final Long idSujeto,
            final LocalDate fechaDesde,
            final LocalDate fechaHasta,
            final int pagina,
            final int tamanoPagina) {

        final var query = new ConsultarLiquidacionesContadorQuery(
                tipo,
                idSujeto,
                fechaDesde,
                fechaHasta,
                Math.max(0, pagina),
                tamanoPagina);

        return liquidacionContableRepository.findAll(query);
    }
}

