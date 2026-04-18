package com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper;

import com.storeinvoice.store_invoice_api.domain.model.FormaPagoCliente;
import com.storeinvoice.store_invoice_api.domain.valueobject.FormaPago;
import com.storeinvoice.store_invoice_api.infrastructure.persistence.entity.FormaPagoClienteJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FormaPagoClienteMapper {

    @Mapping(target = "formaPago", source = "formaPago")
    FormaPagoCliente toDomain(FormaPagoClienteJpaEntity entity);

    @Mapping(target = "formaPago", source = "formaPago")
    FormaPagoClienteJpaEntity toEntity(FormaPagoCliente domain);

    default String formaPagoToString(FormaPago formaPago) {
        return formaPago != null ? formaPago.getValue() : null;
    }

    default FormaPago stringToFormaPago(String value) {
        return FormaPago.fromValue(value);
    }
}