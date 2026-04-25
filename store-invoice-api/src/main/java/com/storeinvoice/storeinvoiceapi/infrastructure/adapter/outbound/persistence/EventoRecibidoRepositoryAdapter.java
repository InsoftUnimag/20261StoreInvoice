package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.persistence;

import com.storeinvoice.storeinvoiceapi.application.repository.EventoRecibidoRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoEvento;
import com.storeinvoice.storeinvoiceapi.domain.model.EventoRecibido;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.EventoRecibidoJpaEntity;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.EventoRecibidoMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de salida (Driven Adapter) que implementa EventoRecibidoRepository usando JPA.
 * Traduce entre el modelo de dominio EventoRecibido y la entidad JPA.
 */
@Repository
@RequiredArgsConstructor
public class EventoRecibidoRepositoryAdapter implements EventoRecibidoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final EventoRecibidoMapper mapper;

    /**
     * Verifica si ya existe un evento con el estado dado para el pedido especificado.
     * Se usa para idempotencia correcta: solo ignora si ya fue PROCESADO exitosamente,
     * permitiendo reintentos de eventos en estado ERROR o PENDIENTE.
     */
    @Override
    public boolean existsByIdPedidoAndEstado(final Long idPedido, final EstadoEvento estado) {
        final String jpql = """
                SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
                FROM EventoRecibidoJpaEntity e
                WHERE e.idPedido = :idPedido AND e.estado = :estado
                """;
        final TypedQuery<Boolean> query = entityManager.createQuery(jpql, Boolean.class);
        query.setParameter("idPedido", idPedido);
        query.setParameter("estado", estado);
        return Boolean.TRUE.equals(query.getSingleResult());
    }

    @Override
    public EventoRecibido save(final EventoRecibido eventoRecibido) {
        final EventoRecibidoJpaEntity entity = mapper.toJpaEntity(eventoRecibido);
        final EventoRecibidoJpaEntity saved = entityManager.merge(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EventoRecibido> findByIdPedido(final Long idPedido) {
        return Optional.ofNullable(entityManager.find(EventoRecibidoJpaEntity.class, idPedido))
                .map(mapper::toDomain);
    }
}
