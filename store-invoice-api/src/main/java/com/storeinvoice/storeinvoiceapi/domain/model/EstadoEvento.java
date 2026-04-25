package com.storeinvoice.storeinvoiceapi.domain.model;

/**
 * Enum de dominio que representa los posibles estados de un EventoRecibido.
 * Usar un tipo fuertemente tipado evita errores por Strings arbitrarios y
 * permite al compilador verificar exhaustividad en switch expressions.
 */
public enum EstadoEvento {
    PENDIENTE,
    PROCESADO,
    ERROR
}

