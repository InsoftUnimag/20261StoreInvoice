package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper;

import com.storeinvoice.storeinvoiceapi.application.dto.response.LiquidacionContadorResponse;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionContable;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR
)
public interface LiquidacionContableMapper {

    LiquidacionContadorResponse toResponse(LiquidacionContable domain);

    List<LiquidacionContadorResponse> toResponseList(List<LiquidacionContable> domains);
}

