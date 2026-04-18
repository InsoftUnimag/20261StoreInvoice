package com.storeinvoice.store_invoice_api.infrastructure.port.outbound;

import com.storeinvoice.store_invoice_api.domain.model.FormaPagoCliente;
import java.util.Optional;

public interface FormaPagoClienteRepositoryPort {

    Optional<FormaPagoCliente> findByIdCliente(Long idCliente);

    FormaPagoCliente save(FormaPagoCliente formaPagoCliente);

    boolean existsByIdCliente(Long idCliente);
}