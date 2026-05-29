package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.mapper;

import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity.FormaPagoClienteJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface FormaPagoClienteJpaMapper {

    FormaPagoCliente toDomain(FormaPagoClienteJpaEntity entity);

    FormaPagoClienteJpaEntity toEntity(FormaPagoCliente domain);
}
