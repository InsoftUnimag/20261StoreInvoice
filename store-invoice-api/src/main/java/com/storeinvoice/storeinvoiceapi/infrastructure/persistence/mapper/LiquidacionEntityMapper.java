package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper;

import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.LiquidacionClienteJpaEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper de persistencia: LiquidacionCliente (dominio) â†”
 * LiquidacionClienteJpaEntity.
 * Responsabilidad Ãºnica: conversiÃ³n entre modelo de dominio y entidad JPA.
 * La conversiÃ³n a Response DTO es responsabilidad de
 * LiquidacionClienteResponseMapper.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface LiquidacionEntityMapper {

    LiquidacionClienteJpaEntity toJpaEntity(LiquidacionCliente domain);

    LiquidacionCliente toDomain(LiquidacionClienteJpaEntity entity);

    List<LiquidacionCliente> toDomainList(List<LiquidacionClienteJpaEntity> entities);
}
