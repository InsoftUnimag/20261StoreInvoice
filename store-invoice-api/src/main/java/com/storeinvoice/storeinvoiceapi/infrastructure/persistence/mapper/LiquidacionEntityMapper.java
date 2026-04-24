package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper;

import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.LiquidacionClienteJpaEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR
)
public interface LiquidacionEntityMapper {

    LiquidacionClienteJpaEntity toJpaEntity(LiquidacionCliente domain);

    LiquidacionCliente toDomain(LiquidacionClienteJpaEntity entity);

    List<LiquidacionCliente> toDomainList(List<LiquidacionClienteJpaEntity> entities);

    LiquidacionClienteResponse toResponse(LiquidacionCliente entity);

    List<LiquidacionClienteResponse> toResponseList(List<LiquidacionCliente> entities);
}
