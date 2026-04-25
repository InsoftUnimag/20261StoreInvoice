package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper;

import com.storeinvoice.storeinvoiceapi.domain.model.EventoRecibido;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.EventoRecibidoJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper de persistencia: EventoRecibido (domain/model) â†” EventoRecibidoJpaEntity.
 * Usa el constructor de reconstrucciÃ³n de EventoRecibido para rehidratar desde BD.
 */
@Component
public class EventoRecibidoMapper {

    public EventoRecibido toDomain(final EventoRecibidoJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new EventoRecibido(
                entity.getIdPedido(),
                entity.getTasaEfectividad(),
                entity.getIdTransportista(),
                entity.getEstado(),
                entity.getFechaRecibido(),
                entity.getFechaProcesado()
        );
    }

    public EventoRecibidoJpaEntity toJpaEntity(final EventoRecibido domain) {
        if (domain == null) {
            return null;
        }
        return new EventoRecibidoJpaEntity(
                domain.getIdPedido(),
                domain.getTasaEfectividad(),
                domain.getIdTransportista(),
                domain.getEstado(),
                domain.getFechaRecibido(),
                domain.getFechaProcesado()
        );
    }
}

