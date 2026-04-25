package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest.mapper;

import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionTransportistaResponse;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR
)
public interface LiquidacionTransportistaResponseMapper {

    LiquidacionTransportistaResponse toResponse(LiquidacionTransportista domain);

    List<LiquidacionTransportistaResponse> toResponseList(List<LiquidacionTransportista> domains);
}

