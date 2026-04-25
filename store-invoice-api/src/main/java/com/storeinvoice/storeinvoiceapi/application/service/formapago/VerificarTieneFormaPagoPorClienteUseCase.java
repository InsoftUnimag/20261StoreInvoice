package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.TieneFormaPagoResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class VerificarTieneFormaPagoPorClienteUseCase {

    private final FormaPagoClienteRepository formaPagoClienteRepository;

    public VerificarTieneFormaPagoPorClienteUseCase(FormaPagoClienteRepository formaPagoClienteRepository) {
        this.formaPagoClienteRepository = formaPagoClienteRepository;
    }

    public TieneFormaPagoResponse ejecutar(Long idCliente) {
        boolean existe = formaPagoClienteRepository.existsByIdCliente(idCliente);
        return new TieneFormaPagoResponse(idCliente, existe);
    }
}

