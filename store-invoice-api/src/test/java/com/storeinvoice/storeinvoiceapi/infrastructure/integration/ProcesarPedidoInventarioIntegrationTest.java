package com.storeinvoice.storeinvoiceapi.infrastructure.integration;

import com.storeinvoice.storeinvoiceapi.StoreInvoiceApiApplication;
import com.storeinvoice.storeinvoiceapi.TestcontainersConfiguration;
import com.storeinvoice.storeinvoiceapi.application.dto.messaging.DatosPedidoInventarioMessage;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente.ProcesarPedidoInventarioUseCase;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de integracion completo del flujo de procesamiento de pedido:
 * 1. ProcesarPedidoInventarioUseCase recibe el mensaje
 * 2. Ejecuta el pipeline reactivo:
 *    - Consulta forma de pago
 *    - Consulta productos (InventarioMockAdapter)
 *    - Consulta cliente (ClienteMockAdapter con id=2)
 *    - Genera PDF (OpenPdfGeneratorAdapter)
 *    - Guarda en BD
 * 3. Verifica que la liquidacion se guardo con URI del PDF
 */
@SpringBootTest(classes = StoreInvoiceApiApplication.class)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProcesarPedidoInventarioIntegrationTest {

    @Autowired
    private ProcesarPedidoInventarioUseCase procesarPedidoUseCase;

    @Autowired
    private FormaPagoClienteRepository formaPagoClienteRepository;

    @Autowired
    private LiquidacionRepository liquidacionRepository;

    @BeforeEach
    void setUp() {
        final FormaPagoCliente formaPago = FormaPagoCliente.builder()
                .idCliente(100L)
                .formaPago(FormaPago.CARTERA_COMERCIAL)
                .fechaRegistro(LocalDateTime.now())
                .build();
        formaPagoClienteRepository.save(formaPago);
    }

    @AfterEach
    void tearDown() {
        liquidacionRepository.findByIdPedido(200L).ifPresent(l -> {
            // No hay metodo delete, solo limpiamos para el siguiente test
        });
    }

    @Test
    void ejecutar_flujoCompleto_generaPdfYGuardaLiquidacion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(200L, 2L, 8500L, "Carrera 10 # 20-30");

        StepVerifier.create(procesarPedidoUseCase.ejecutar(mensaje))
                .verifyComplete();

        final Optional<LiquidacionCliente> guardada = liquidacionRepository.findByIdPedido(200L);
        assertTrue(guardada.isPresent(), "La liquidacion debe haberse guardado en BD");
        assertEquals(200L, guardada.get().getIdPedido());
        assertEquals(100L, guardada.get().getIdCliente());
        assertEquals(FormaPago.CARTERA_COMERCIAL, guardada.get().getFormaPago());
        assertNotNull(guardada.get().getUriPdf(), "La URI del PDF no debe ser nula");
        assertTrue(guardada.get().getUriPdf().contains("liquidacion-pedido-200-"),
                "La URI debe contener el nombre del archivo");
    }

    @Test
    void ejecutar_mensajeInvalido_noGuardaEnBd() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(0L, 2L, 1000L, "Calle");

        StepVerifier.create(procesarPedidoUseCase.ejecutar(mensaje))
                .verifyComplete();

        final Optional<LiquidacionCliente> guardada = liquidacionRepository.findByIdPedido(0L);
        assertTrue(guardada.isEmpty(), "No debe guardar liquidacion con idPedido invalido");
    }
}

