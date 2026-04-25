package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.persistence;

import com.storeinvoice.storeinvoiceapi.application.repository.PedidoRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.Pedido;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.PedidoJpaEntity;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.PedidoJpaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de salida (Driven Adapter) que implementa PedidoRepository usando JPA.
 * Traduce entre el modelo de dominio Pedido y la entidad JPA PedidoJpaEntity.
 */
@Repository
@RequiredArgsConstructor
public class PedidoRepositoryAdapter implements PedidoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final PedidoJpaMapper pedidoMapper;

    @Override
    public Pedido save(final Pedido pedido) {
        final PedidoJpaEntity entity = pedidoMapper.toJpaEntity(pedido);
        final PedidoJpaEntity saved = entityManager.merge(entity);
        return pedidoMapper.toDomain(saved);
    }

    @Override
    public Optional<BigDecimal> findPrecioPedidoByIdPedido(final Long idPedido) {
        final String jpql = "SELECT p.precioTotal FROM PedidoJpaEntity p WHERE p.idPedido = :idPedido";
        final TypedQuery<BigDecimal> query = entityManager.createQuery(jpql, BigDecimal.class);
        query.setParameter("idPedido", idPedido);
        final var result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.ofNullable(result.get(0));
    }

    @Override
    public Optional<Pedido> findById(final Long idPedido) {
        return Optional.ofNullable(entityManager.find(PedidoJpaEntity.class, idPedido))
                .map(pedidoMapper::toDomain);
    }
}
