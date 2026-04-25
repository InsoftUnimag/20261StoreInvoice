package com.storeinvoice.storeinvoiceapi.application.service.formapago;

import com.storeinvoice.storeinvoiceapi.application.dto.response.FormaPagoClienteResponse;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoAlreadyExistsException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarFormaPagoClienteUseCaseTest {

    @Mock
    private FormaPagoClienteRepository formaPagoClienteRepository;

    @InjectMocks
    private RegistrarFormaPagoClienteUseCase useCase;

    private FormaPagoCliente formaPagoClienteGuardado;

    @BeforeEach
    void setUp() {
        formaPagoClienteGuardado = FormaPagoCliente.builder()
                .idCliente(1L)
                .formaPago(FormaPago.CONTRA_ENTREGA)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    @Test
    void ejecutar_formaPagoValida_registraExitosamente() {
        when(formaPagoClienteRepository.existsByIdCliente(1L)).thenReturn(false);
        when(formaPagoClienteRepository.save(any(FormaPagoCliente.class))).thenReturn(formaPagoClienteGuardado);

        useCase.ejecutar(1L, FormaPago.CONTRA_ENTREGA)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1L, result.idCliente());
                    assertEquals("CONTRA_ENTREGA", result.formaPago());
                })
                .verifyComplete();

        verify(formaPagoClienteRepository).save(any(FormaPagoCliente.class));
    }

    @Test
    void ejecutar_clienteYaTieneFormaPago_lanzaExcepcion() {
        when(formaPagoClienteRepository.existsByIdCliente(1L)).thenReturn(true);

        useCase.ejecutar(1L, FormaPago.CONTRA_ENTREGA)
                .as(StepVerifier::create)
                .expectError(FormaPagoAlreadyExistsException.class)
                .verify();

        verify(formaPagoClienteRepository, never()).save(any());
    }

    @Test
    void ejecutar_carteraComercial_registraExitosamente() {
        FormaPagoCliente clienteCartera = FormaPagoCliente.builder()
                .idCliente(2L)
                .formaPago(FormaPago.CARTERA_COMERCIAL)
                .fechaRegistro(LocalDateTime.now())
                .build();

        when(formaPagoClienteRepository.existsByIdCliente(2L)).thenReturn(false);
        when(formaPagoClienteRepository.save(any(FormaPagoCliente.class))).thenReturn(clienteCartera);

        useCase.ejecutar(2L, FormaPago.CARTERA_COMERCIAL)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(2L, result.idCliente());
                    assertEquals("CARTERA_COMERCIAL", result.formaPago());
                })
                .verifyComplete();
    }
}

