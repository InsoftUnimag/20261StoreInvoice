package com.storeinvoice.storeinvoiceapi.domain.entity;

/**
 * @deprecated Este archivo ha sido REEMPLAZADO por
 * {@link com.storeinvoice.storeinvoiceapi.domain.model.EventoRecibido}.
 *
 * <p>El paquete {@code domain.entity} no forma parte de la arquitectura hexagonal definida en el plan.
 * El modelo de dominio EventoRecibido fue movido a {@code domain.model} donde corresponde,
 * con diseÃ±o inmutable (sin @Setter libre), estado tipado con {@code EstadoEvento} enum,
 * y mÃ©todos de comportamiento {@code marcarProcesado()} / {@code marcarError()}.
 *
 * <p>Este archivo puede eliminarse de forma segura una vez que no existan referencias a Ã©l.
 */
@Deprecated(since = "refactoring-hexagonal", forRemoval = true)
public class EventoRecibido {
    // Clase vacÃ­a - toda la lÃ³gica fue movida a domain.model.EventoRecibido
    // NO USAR - este tipo no es compatible con el sistema actual
}

