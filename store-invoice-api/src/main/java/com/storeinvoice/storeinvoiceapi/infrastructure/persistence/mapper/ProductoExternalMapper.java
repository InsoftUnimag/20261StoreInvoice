package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper;

import com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external.dto.product.ProductoExternalDTO;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper de MapStruct para transformar DTOs externos del MÃ³dulo de Inventario
 * al modelo puro de Dominio. ActÃºa como barrera de anti-corrupciÃ³n que
 * impide que los DTOs externos contaminen las capas internas.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR
)
public interface ProductoExternalMapper {

    Producto toDomain(ProductoExternalDTO dto);
}

