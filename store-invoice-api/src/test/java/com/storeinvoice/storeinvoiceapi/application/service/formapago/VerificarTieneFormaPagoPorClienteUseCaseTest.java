package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.TieneFormaPagoResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarTieneFormaPagoPorClienteUseCaseTest {

    @Mock
    private FormaPagoClienteRepository formaPagoClienteRepository;

    private VerificarTieneFormaPagoPorClienteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new VerificarTieneFormaPagoPorClienteUseCase(formaPagoClienteRepository);
    }

    @Test
    void ejecutar_clienteConFormaPago_retornaTrue() {
        when(formaPagoClienteRepository.existsByIdCliente(1L)).thenReturn(true);

        TieneFormaPagoResponse result = useCase.ejecutar(1L);

        assertNotNull(result);
        assertEquals(1L, result.idCliente());
        assertTrue(result.tieneFormaPago());
    }

    @Test
    void ejecutar_clienteSinFormaPago_retornaFalse() {
        when(formaPagoClienteRepository.existsByIdCliente(999L)).thenReturn(false);

        TieneFormaPagoResponse result = useCase.ejecutar(999L);

        assertNotNull(result);
        assertEquals(999L, result.idCliente());
        assertFalse(result.tieneFormaPago());
    }
}
