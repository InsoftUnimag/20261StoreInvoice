package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.FormaPagoClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarFormaPagoPorClienteUseCaseTest {

    @Mock
    private FormaPagoClienteRepository formaPagoClienteRepository;

    private ConsultarFormaPagoPorClienteUseCase useCase;

    private FormaPagoCliente formaPagoCliente;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarFormaPagoPorClienteUseCase(formaPagoClienteRepository);

        formaPagoCliente = FormaPagoCliente.builder()
                .idCliente(1L)
                .formaPago(FormaPago.CARTERA_COMERCIAL)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    @Test
    void ejecutar_clienteConFormaPago_retornaFormaPago() {
        when(formaPagoClienteRepository.existsByIdCliente(1L)).thenReturn(true);
        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.of(formaPagoCliente));

        useCase.ejecutar(1L)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1L, result.idCliente());
                    assertEquals("CARTERA_COMERCIAL", result.formaPago());
                })
                .verifyComplete();
    }

    @Test
    void ejecutar_clienteNoExistente_lanzaExcepcion() {
        when(formaPagoClienteRepository.existsByIdCliente(999L)).thenReturn(false);

        useCase.ejecutar(999L)
                .as(StepVerifier::create)
                .expectError(ClienteNotFoundException.class)
                .verify();
    }

    @Test
    void ejecutar_clienteSinFormaPago_lanzaExcepcion() {
        when(formaPagoClienteRepository.existsByIdCliente(1L)).thenReturn(true);
        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.empty());

        useCase.ejecutar(1L)
                .as(StepVerifier::create)
                .expectError(FormaPagoNotFoundException.class)
                .verify();
    }
}

