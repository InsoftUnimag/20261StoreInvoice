package com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper;

import com.storeinvoice.store_invoice_api.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.store_invoice_api.domain.model.LiquidacionCliente;
import com.storeinvoice.store_invoice_api.infrastructure.persistence.entity.LiquidacionClienteJpaEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR
)
public interface LiquidacionEntityMapper {

    LiquidacionCliente toDomain(LiquidacionClienteJpaEntity entity);

    List<LiquidacionCliente> toDomainList(List<LiquidacionClienteJpaEntity> entities);

    LiquidacionClienteResponse toResponse(LiquidacionCliente entity);

    List<LiquidacionClienteResponse> toResponseList(List<LiquidacionCliente> entities);
}