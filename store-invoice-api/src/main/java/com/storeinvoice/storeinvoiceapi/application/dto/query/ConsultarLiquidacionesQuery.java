package com.storeinvoice.storeinvoiceapi.application.dto.query;

public record ConsultarLiquidacionesQuery(
        Long idCliente,
        int pagina,
        int tamañoPagina
) {
    public int offset() {
        return pagina * tamañoPagina;
    }
}