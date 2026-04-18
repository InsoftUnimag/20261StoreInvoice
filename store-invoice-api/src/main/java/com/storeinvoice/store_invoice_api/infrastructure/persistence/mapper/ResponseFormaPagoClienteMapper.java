package com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper;

import com.storeinvoice.store_invoice_api.domain.model.FormaPagoCliente;
import com.storeinvoice.store_invoice_api.domain.valueobject.FormaPago;
import com.storeinvoice.store_invoice_api.application.dto.response.FormaPagoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ResponseFormaPagoClienteMapper {

    default FormaPagoResponse toResponse(FormaPagoCliente formaPagoCliente) {
        return new FormaPagoResponse(
            formaPagoCliente.getIdCliente(),
            formaPagoCliente.getFormaPago().getValue(),
            formaPagoCliente.getFechaRegistro()
        );
    }
}