package com.storeinvoice.infrastructure.adapter.outbound.persistence;

import com.storeinvoice.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.domain.model.LiquidacionCliente;
import com.storeinvoice.infrastructure.persistence.entity.LiquidacionClienteJpaEntity;
import com.storeinvoice.infrastructure.persistence.mapper.LiquidacionEntityMapper;
import com.storeinvoice.infrastructure.port.outbound.LiquidacionRepositoryPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public final class LiquidacionRepositoryAdapter implements LiquidacionRepositoryPort {

    @PersistenceContext
    private final EntityManager entityManager;

    public LiquidacionRepositoryAdapter(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<LiquidacionCliente> findByIdCliente(final ConsultarLiquidacionesQuery query) {
        final String jpql = "SELECT l FROM LiquidacionClienteJpaEntity l WHERE l.idCliente = :idCliente ORDER BY l.fechaLiquidacion DESC";
        final TypedQuery<LiquidacionClienteJpaEntity> typedQuery = entityManager.createQuery(jpql, LiquidacionClienteJpaEntity.class);
        typedQuery.setParameter("idCliente", query.getIdCliente());
        typedQuery.setFirstResult(query.getOffset());
        typedQuery.setMaxResults(query.getTamañoPagina());
        
        return LiquidacionEntityMapper.toDomainList(typedQuery.getResultList());
    }

    @Override
    public long countByIdCliente(final Long idCliente) {
        final String jpql = "SELECT COUNT(l) FROM LiquidacionClienteJpaEntity l WHERE l.idCliente = :idCliente";
        final TypedQuery<Long> typedQuery = entityManager.createQuery(jpql, Long.class);
        typedQuery.setParameter("idCliente", idCliente);
        return Optional.ofNullable(typedQuery.getSingleResult()).orElse(0L);
    }
}