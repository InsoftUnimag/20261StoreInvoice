package com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external;

import com.storeinvoice.store_invoice_api.application.dto.client.ClienteClientResponse;
import com.storeinvoice.store_invoice_api.infrastructure.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "clienteServiceClient",
    url = "${external.cliente.module.base-url:http://localhost:8081}",
    configuration = FeignClientConfig.class
)
public interface ClienteServiceClient {
    
    @GetMapping("/api/v1/clientes/{id_nacional}")
    ResponseEntity<ClienteClientResponse> consultarClientePorIdNacional(
            @PathVariable("id_nacional") String idNacional);
    
    @GetMapping("/api/v1/clientes/{id_cliente}")
    ResponseEntity<ClienteClientResponse> consultarClientePorIdCliente(
            @PathVariable("id_cliente") String idCliente);
}