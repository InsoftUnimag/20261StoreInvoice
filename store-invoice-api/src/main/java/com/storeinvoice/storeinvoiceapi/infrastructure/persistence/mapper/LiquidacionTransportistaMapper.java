package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper;

import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionTransportistaResponse;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.LiquidacionTransportistaJpaEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR
)
public interface LiquidacionTransportistaMapper {

    LiquidacionTransportistaJpaEntity toJpaEntity(LiquidacionTransportista domain);

    LiquidacionTransportista toDomain(LiquidacionTransportistaJpaEntity entity);

    List<LiquidacionTransportista> toDomainList(List<LiquidacionTransportistaJpaEntity> entities);

    LiquidacionTransportistaResponse toResponse(LiquidacionTransportista domain);

    List<LiquidacionTransportistaResponse> toResponseList(List<LiquidacionTransportista> domains);
}
