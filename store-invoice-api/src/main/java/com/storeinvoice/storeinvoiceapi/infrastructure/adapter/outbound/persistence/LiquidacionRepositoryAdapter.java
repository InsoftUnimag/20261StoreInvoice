package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.persistence;

import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesQuery;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.LiquidacionClienteJpaEntity;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.LiquidacionEntityMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class LiquidacionRepositoryAdapter implements LiquidacionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final LiquidacionEntityMapper liquidacionMapper;

    public LiquidacionRepositoryAdapter(final LiquidacionEntityMapper liquidacionMapper) {
        this.liquidacionMapper = liquidacionMapper;
    }

    @Override
    public List<LiquidacionCliente> findByIdCliente(final ConsultarLiquidacionesQuery query) {
        final String jpql = "SELECT l FROM LiquidacionClienteJpaEntity l WHERE l.idCliente = :idCliente ORDER BY l.fechaLiquidacion DESC";
        final TypedQuery<LiquidacionClienteJpaEntity> typedQuery = entityManager.createQuery(jpql, LiquidacionClienteJpaEntity.class);
        typedQuery.setParameter("idCliente", query.idCliente());
        typedQuery.setFirstResult(query.offset());
        typedQuery.setMaxResults(query.tamanoPagina());
        
        return liquidacionMapper.toDomainList(typedQuery.getResultList());
    }

    @Override
    public long countByIdCliente(final Long idCliente) {
        final String jpql = "SELECT COUNT(l) FROM LiquidacionClienteJpaEntity l WHERE l.idCliente = :idCliente";
        final TypedQuery<Long> typedQuery = entityManager.createQuery(jpql, Long.class);
        typedQuery.setParameter("idCliente", idCliente);
        return Optional.ofNullable(typedQuery.getSingleResult()).orElse(0L);
    }

    @Override
    public boolean existsByIdCliente(final Long idCliente) {
        final String jpql = "SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LiquidacionClienteJpaEntity l WHERE l.idCliente = :idCliente";
        final TypedQuery<Boolean> typedQuery = entityManager.createQuery(jpql, Boolean.class);
        typedQuery.setParameter("idCliente", idCliente);
        return Optional.ofNullable(typedQuery.getSingleResult()).orElse(false);
    }

    @Override
    public Optional<BigDecimal> findMontoLiquidadoByIdPedido(final Long idPedido) {
        final String jpql = "SELECT l.montoLiquidado FROM LiquidacionClienteJpaEntity l WHERE l.idPedido = :idPedido";
        final TypedQuery<BigDecimal> typedQuery = entityManager.createQuery(jpql, BigDecimal.class);
        typedQuery.setParameter("idPedido", idPedido);
        final var result = typedQuery.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}