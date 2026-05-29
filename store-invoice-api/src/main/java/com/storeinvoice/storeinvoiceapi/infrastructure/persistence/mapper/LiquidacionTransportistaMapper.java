package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper;

import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.LiquidacionTransportistaJpaEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper de persistencia: LiquidacionTransportista (dominio) â†”
 * LiquidacionTransportistaJpaEntity.
 * Responsabilidad Ãºnica: conversiÃ³n entre modelo de dominio y entidad JPA.
 * La conversiÃ³n a Response DTO es responsabilidad de
 * LiquidacionTransportistaResponseMapper.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface LiquidacionTransportistaMapper {

    LiquidacionTransportistaJpaEntity toJpaEntity(LiquidacionTransportista domain);

    LiquidacionTransportista toDomain(LiquidacionTransportistaJpaEntity entity);

    List<LiquidacionTransportista> toDomainList(List<LiquidacionTransportistaJpaEntity> entities);
}
