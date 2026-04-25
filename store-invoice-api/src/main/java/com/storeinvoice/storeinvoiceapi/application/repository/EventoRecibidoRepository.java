package com.storeinvoice.storeinvoiceapi.application.repository;

import com.storeinvoice.storeinvoiceapi.domain.model.EstadoEvento;
import com.storeinvoice.storeinvoiceapi.domain.model.EventoRecibido;
import java.util.Optional;

/**
 * Puerto de salida (Outbound Port) para persistencia de EventoRecibido.
 * Define el contrato puro de repositorio sin dependencias de tecnología.
 */
public interface EventoRecibidoRepository {

    /**
     * Verifica si ya existe un evento procesado exitosamente para el pedido dado.
     * Solo retorna true cuando el evento tiene estado PROCESADO, permitiendo
     * reintentos legítimos de eventos en estado ERROR o PENDIENTE.
     *
     * @param idPedido ID del pedido a verificar
     * @return true si el evento fue procesado exitosamente (idempotencia correcta)
     */
    boolean existsByIdPedidoAndEstado(Long idPedido, EstadoEvento estado);

    EventoRecibido save(EventoRecibido eventoRecibido);

    Optional<EventoRecibido> findByIdPedido(Long idPedido);
}
