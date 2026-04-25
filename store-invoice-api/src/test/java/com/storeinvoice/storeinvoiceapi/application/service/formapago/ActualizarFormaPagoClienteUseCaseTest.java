package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.FormaPagoClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActualizarFormaPagoClienteUseCaseTest {

    @Mock
    private FormaPagoClienteRepository formaPagoClienteRepository;

    @InjectMocks
    private ActualizarFormaPagoClienteUseCase useCase;

    private FormaPagoCliente formaPagoClienteExistente;

    @BeforeEach
    void setUp() {
        formaPagoClienteExistente = FormaPagoCliente.builder()
                .idCliente(1L)
                .formaPago(FormaPago.CONTRA_ENTREGA)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    @Test
    void ejecutar_formaPagoValida_actualizaExitosamente() {
        FormaPagoCliente clienteActualizado = FormaPagoCliente.builder()
                .idCliente(1L)
                .formaPago(FormaPago.CARTERA_COMERCIAL)
                .fechaRegistro(LocalDateTime.now())
                .build();

        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.of(formaPagoClienteExistente));
        when(formaPagoClienteRepository.update(any(FormaPagoCliente.class))).thenReturn(clienteActualizado);

        useCase.ejecutar(1L, FormaPago.CARTERA_COMERCIAL)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1L, result.idCliente());
                    assertEquals("CARTERA_COMERCIAL", result.formaPago());
                })
                .verifyComplete();

        verify(formaPagoClienteRepository).update(any(FormaPagoCliente.class));
    }

    @Test
    void ejecutar_clienteNoExiste_lanzaExcepcion() {
        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.empty());

        useCase.ejecutar(1L, FormaPago.CONTRA_ENTREGA)
                .as(StepVerifier::create)
                .expectError(FormaPagoNotFoundException.class)
                .verify();

        verify(formaPagoClienteRepository, never()).update(any());
    }

    @Test
    void ejecutar_cambiarDeContraEntregaACartera_actualizaExitosamente() {
        FormaPagoCliente clienteActualizado = FormaPagoCliente.builder()
                .idCliente(1L)
                .formaPago(FormaPago.CARTERA_COMERCIAL)
                .fechaRegistro(LocalDateTime.now())
                .build();

        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.of(formaPagoClienteExistente));
        when(formaPagoClienteRepository.update(any(FormaPagoCliente.class))).thenReturn(clienteActualizado);

        useCase.ejecutar(1L, FormaPago.CARTERA_COMERCIAL)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertEquals("CARTERA_COMERCIAL", result.formaPago());
                })
                .verifyComplete();
    }

    @Test
    void ejecutar_cambiarDeCarteraAContraEntrega_actualizaExitosamente() {
        FormaPagoCliente clienteConCartera = FormaPagoCliente.builder()
                .idCliente(1L)
                .formaPago(FormaPago.CARTERA_COMERCIAL)
                .fechaRegistro(LocalDateTime.now())
                .build();

        FormaPagoCliente clienteActualizado = FormaPagoCliente.builder()
                .idCliente(1L)
                .formaPago(FormaPago.CONTRA_ENTREGA)
                .fechaRegistro(LocalDateTime.now())
                .build();

        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.of(clienteConCartera));
        when(formaPagoClienteRepository.update(any(FormaPagoCliente.class))).thenReturn(clienteActualizado);

        useCase.ejecutar(1L, FormaPago.CONTRA_ENTREGA)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertEquals("CONTRA_ENTREGA", result.formaPago());
                })
                .verifyComplete();
    }
}
