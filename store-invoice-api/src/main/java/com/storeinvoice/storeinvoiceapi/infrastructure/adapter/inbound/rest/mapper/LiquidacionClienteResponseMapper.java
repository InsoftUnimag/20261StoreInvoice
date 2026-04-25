package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest.mapper;

import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionClienteResponse;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper de presentación: LiquidacionCliente (dominio) → LiquidacionClienteResponse (DTO REST).
 * Responsabilidad única: conversión de modelo de dominio a DTO de respuesta REST.
 * Reside en la capa de infraestructura inbound (REST), no en la capa de persistencia.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR
)
public interface LiquidacionClienteResponseMapper {

    LiquidacionClienteResponse toResponse(LiquidacionCliente domain);

    List<LiquidacionClienteResponse> toResponseList(List<LiquidacionCliente> domains);
}
