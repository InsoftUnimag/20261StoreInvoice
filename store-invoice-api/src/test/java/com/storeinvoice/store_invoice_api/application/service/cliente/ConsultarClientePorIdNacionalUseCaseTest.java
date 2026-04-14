package com.storeinvoice.store_invoice_api.application.service.cliente;

import com.storeinvoice.store_invoice_api.application.dto.client.ClienteClientResponse;
import com.storeinvoice.store_invoice_api.application.dto.response.ClienteResponse;
import com.storeinvoice.store_invoice_api.domain.exception.ClienteNotFoundException;
import com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external.ClienteServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarClientePorIdNacionalUseCaseTest {

    @Mock
    private ClienteServiceClient clienteServiceClient;

    @InjectMocks
    private ConsultarClientePorIdNacionalUseCase useCase;

    private ClienteClientResponse clienteResponse;

    @BeforeEach
    void setUp() {
        clienteResponse = new ClienteClientResponse(
                "100",
                "12345678",
                "Juan Perez",
                "3001234567",
                "Calle 123 #45-67"
        );
    }

    @Test
    void consultarClientePorIdNacional_exitoso_retornaCliente() {
        when(clienteServiceClient.consultarClientePorIdNacional("12345678"))
                .thenReturn(new ResponseEntity<>(clienteResponse, HttpStatus.OK));

        ClienteResponse result = useCase.consultarClientePorIdNacional("12345678");

        assertNotNull(result);
        assertEquals("100", result.idCliente());
        assertEquals("12345678", result.idNacional());
        assertEquals("Juan Perez", result.nombre());
        verify(clienteServiceClient).consultarClientePorIdNacional("12345678");
    }

    @Test
    void consultarClientePorIdNacional_clienteNoExistente_lanzaExcepcion() {
        when(clienteServiceClient.consultarClientePorIdNacional("99999999"))
                .thenReturn(ResponseEntity.notFound().build());

        assertThrows(ClienteNotFoundException.class, () -> 
                useCase.consultarClientePorIdNacional("99999999"));
    }

    @Test
    void consultarClientePorIdNacional_idNacionalVacio_lanzaExcepcion() {
        assertThrows(ClienteNotFoundException.class, () -> 
                useCase.consultarClientePorIdNacional(""));
    }

    @Test
    void consultarClientePorIdNacional_idNacionalNull_lanzaExcepcion() {
        assertThrows(ClienteNotFoundException.class, () -> 
                useCase.consultarClientePorIdNacional(null));
    }

    @Test
    void consultarClientePorIdCliente_exitoso_retornaCliente() {
        when(clienteServiceClient.consultarClientePorIdCliente("100"))
                .thenReturn(new ResponseEntity<>(clienteResponse, HttpStatus.OK));

        ClienteResponse result = useCase.consultarClientePorIdCliente("100");

        assertNotNull(result);
        assertEquals("100", result.idCliente());
        assertEquals("Juan Perez", result.nombre());
    }

    @Test
    void consultarClientePorIdCliente_clienteNoExistente_lanzaExcepcion() {
        when(clienteServiceClient.consultarClientePorIdCliente("999"))
                .thenReturn(ResponseEntity.notFound().build());

        assertThrows(ClienteNotFoundException.class, () -> 
                useCase.consultarClientePorIdCliente("999"));
    }
}