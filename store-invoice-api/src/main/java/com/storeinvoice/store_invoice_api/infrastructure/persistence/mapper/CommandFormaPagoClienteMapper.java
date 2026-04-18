package com.storeinvoice.store_invoice_api.infrastructure.persistence.mapper;

import com.storeinvoice.store_invoice_api.domain.model.FormaPagoCliente;
import com.storeinvoice.store_invoice_api.domain.valueobject.FormaPago;
import com.storeinvoice.store_invoice_api.application.dto.command.RegistrarFormaPagoCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommandFormaPagoClienteMapper {

    @Mapping(target = "formaPago", source = "formaPago")
    default FormaPagoCliente toDomain(RegistrarFormaPagoCommand command) {
        return new FormaPagoCliente(
            command.idCliente(),
            FormaPago.fromValue(command.formaPago()),
            LocalDateTime.now()
        );
    }
}