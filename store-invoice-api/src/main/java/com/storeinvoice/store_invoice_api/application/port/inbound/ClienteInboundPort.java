package com.storeinvoice.store_invoice_api.application.port.inbound;

import com.storeinvoice.store_invoice_api.application.dto.response.ClienteResponse;

public interface ClienteInboundPort {
    
    ClienteResponse consultarClientePorIdNacional(String idNacional);
    
    ClienteResponse consultarClientePorIdCliente(String idCliente);
}