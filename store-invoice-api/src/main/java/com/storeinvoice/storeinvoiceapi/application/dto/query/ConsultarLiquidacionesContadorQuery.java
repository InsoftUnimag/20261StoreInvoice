package com.storeinvoice.storeinvoiceapi.application.dto.query;

import java.time.LocalDate;

public record ConsultarLiquidacionesContadorQuery(
        String tipo,
        Long idSujeto,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        int pagina,
        int tamanoPagina
) {
    public int offset() {
        return pagina * tamanoPagina;
    }
}

