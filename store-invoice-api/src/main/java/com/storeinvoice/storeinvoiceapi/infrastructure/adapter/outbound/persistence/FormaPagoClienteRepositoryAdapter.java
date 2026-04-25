package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.persistence;

import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.FormaPagoClienteJpaEntity;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper.FormaPagoClienteJpaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class FormaPagoClienteRepositoryAdapter implements FormaPagoClienteRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final FormaPagoClienteJpaMapper mapper;

    public FormaPagoClienteRepositoryAdapter(final FormaPagoClienteJpaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Optional<FormaPagoCliente> findByIdCliente(Long idCliente) {
        final String jpql = "SELECT f FROM FormaPagoClienteJpaEntity f WHERE f.idCliente = :idCliente";
        final TypedQuery<FormaPagoClienteJpaEntity> typedQuery = entityManager.createQuery(jpql, FormaPagoClienteJpaEntity.class);
        typedQuery.setParameter("idCliente", idCliente);
        typedQuery.setMaxResults(1);
        final var result = typedQuery.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(result.get(0)));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdCliente(Long idCliente) {
        final String jpql = "SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FormaPagoClienteJpaEntity f WHERE f.idCliente = :idCliente";
        final TypedQuery<Boolean> typedQuery = entityManager.createQuery(jpql, Boolean.class);
        typedQuery.setParameter("idCliente", idCliente);
        return Optional.ofNullable(typedQuery.getSingleResult()).orElse(false);
    }

    @Override
    @Transactional
    public FormaPagoCliente save(FormaPagoCliente formaPagoCliente) {
        final FormaPagoClienteJpaEntity entity = mapper.toEntity(formaPagoCliente);
        entityManager.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional
    public FormaPagoCliente update(FormaPagoCliente formaPagoCliente) {
        final FormaPagoClienteJpaEntity entity = mapper.toEntity(formaPagoCliente);
        final FormaPagoClienteJpaEntity mergedEntity = entityManager.merge(entity);
        return mapper.toDomain(mergedEntity);
    }
}
