package com.storeinvoice.storeinvoiceapi.application.repository;

import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import java.util.Optional;

public interface FormaPagoClienteRepository {

    Optional<FormaPagoCliente> findByIdCliente(Long idCliente);

    boolean existsByIdCliente(Long idCliente);

    FormaPagoCliente save(FormaPagoCliente formaPagoCliente);

    FormaPagoCliente update(FormaPagoCliente formaPagoCliente);
}

