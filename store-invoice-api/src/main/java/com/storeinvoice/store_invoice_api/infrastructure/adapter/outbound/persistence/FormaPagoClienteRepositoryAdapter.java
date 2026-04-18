package com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.persistence;

import com.storeinvoice.store_invoice_api.domain.model.FormaPagoCliente;
import com.storeinvoice.store_invoice_api.infrastructure.persistence.entity.FormaPagoClienteJpaEntity;
import com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper.FormaPagoClienteMapper;
import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.FormaPagoClienteRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public class FormaPagoClienteRepositoryAdapter implements FormaPagoClienteRepositoryPort {

    private static final Logger LOG = LoggerFactory.getLogger(FormaPagoClienteRepositoryAdapter.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final FormaPagoClienteMapper formaPagoClienteMapper;

    public FormaPagoClienteRepositoryAdapter(FormaPagoClienteMapper formaPagoClienteMapper) {
        this.formaPagoClienteMapper = formaPagoClienteMapper;
    }

    @Override
    public Optional<FormaPagoCliente> findByIdCliente(Long idCliente) {
        FormaPagoClienteJpaEntity entity = entityManager.find(FormaPagoClienteJpaEntity.class, idCliente);
        return Optional.ofNullable(entity).map(formaPagoClienteMapper::toDomain);
    }

    @Override
    @Transactional
    public FormaPagoCliente save(FormaPagoCliente formaPagoCliente) {
        FormaPagoClienteJpaEntity entity = formaPagoClienteMapper.toEntity(formaPagoCliente);
        if (entityManager.find(FormaPagoClienteJpaEntity.class, entity.getIdCliente()) != null) {
            entityManager.merge(entity);
        } else {
            entityManager.persist(entity);
        }
        return formaPagoCliente;
    }

    @Override
    public boolean existsByIdCliente(Long idCliente) {
        return entityManager.find(FormaPagoClienteJpaEntity.class, idCliente) != null;
    }
}