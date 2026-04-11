package com.storeinvoice.application.dto.query;

public final class ConsultarLiquidacionesQuery {

    private final Long idCliente;
    private final int pagina;
    private final int tamañoPagina;

    public ConsultarLiquidacionesQuery(final Long idCliente, final int pagina, final int tamañoPagina) {
        this.idCliente = idCliente;
        this.pagina = pagina;
        this.tamañoPagina = tamañoPagina;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public int getPagina() {
        return pagina;
    }

    public int getTamañoPagina() {
        return tamañoPagina;
    }

    public int getOffset() {
        return pagina * tamañoPagina;
    }
}