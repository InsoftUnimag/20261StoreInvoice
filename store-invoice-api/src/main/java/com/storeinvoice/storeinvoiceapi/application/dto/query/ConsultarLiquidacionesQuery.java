package com.storeinvoice.storeinvoiceapi.application.dto.query;

public record ConsultarLiquidacionesQuery(
        Long idCliente,
        int pagina,
        int tamanoPagina
) {
    public int offset() {
        return pagina * tamanoPagina;
    }
}
