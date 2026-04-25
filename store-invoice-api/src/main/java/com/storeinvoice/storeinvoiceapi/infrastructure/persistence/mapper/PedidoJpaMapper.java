package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper;

import com.storeinvoice.storeinvoiceapi.domain.model.Pedido;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.PedidoJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper de persistencia: Pedido (dominio) ↔ PedidoJpaEntity (JPA).
 * Responsabilidad única: conversión entre modelo de dominio y entidad JPA.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR
)
public interface PedidoJpaMapper {

    PedidoJpaEntity toJpaEntity(Pedido domain);

    Pedido toDomain(PedidoJpaEntity entity);
}
