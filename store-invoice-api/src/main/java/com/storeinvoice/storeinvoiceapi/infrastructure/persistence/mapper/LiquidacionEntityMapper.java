package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper;

import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.LiquidacionClienteJpaEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper de persistencia: LiquidacionCliente (dominio) ↔ LiquidacionClienteJpaEntity.
 * Responsabilidad única: conversión entre modelo de dominio y entidad JPA.
 * La conversión a Response DTO es responsabilidad de LiquidacionClienteResponseMapper.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR
)
public interface LiquidacionEntityMapper {

    LiquidacionClienteJpaEntity toJpaEntity(LiquidacionCliente domain);

    LiquidacionCliente toDomain(LiquidacionClienteJpaEntity entity);

    List<LiquidacionCliente> toDomainList(List<LiquidacionClienteJpaEntity> entities);
}