package com.storeinvoice.storeinvoiceapi.infrastructure.integration;

import com.storeinvoice.storeinvoiceapi.StoreInvoiceApiApplication;
import com.storeinvoice.storeinvoiceapi.TestcontainersConfiguration;
import com.storeinvoice.storeinvoiceapi.application.dto.messaging.DatosPedidoInventarioMessage;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente.RegistrarLiquidacionDesdeInventarioUseCase;
import com.storeinvoice.storeinvoiceapi.domain.exception.DatosPedidoInvalidosException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoClienteNoEncontradaException;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = StoreInvoiceApiApplication.class)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PedidoRecepcionIntegrationTest {

    @Autowired
    private RegistrarLiquidacionDesdeInventarioUseCase useCase;

    @Autowired
    private FormaPagoClienteRepository formaPagoClienteRepository;

    @Autowired
    private LiquidacionRepository liquidacionRepository;

    @BeforeEach
    void setUp() {
        final FormaPagoCliente formaPago = FormaPagoCliente.builder()
                .idCliente(1L)
                .formaPago(FormaPago.CARTERA_COMERCIAL)
                .fechaRegistro(LocalDateTime.now())
                .build();
        formaPagoClienteRepository.save(formaPago);
    }

    @Test
    void ejecutar_mensajeValido_guardaLiquidacionEnBd() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");

        final LiquidacionCliente resultado = useCase.ejecutar(mensaje);

        assertNotNull(resultado);
        assertNotNull(resultado.getIdLiquidacion());
        assertEquals(100L, resultado.getIdPedido());
        assertEquals(1L, resultado.getIdCliente());
        assertEquals(FormaPago.CARTERA_COMERCIAL, resultado.getFormaPago());
        assertEquals(EstadoLiquidacion.PENDIENTE, resultado.getEstadoLiquidacion());

        final Optional<LiquidacionCliente> guardada = liquidacionRepository.findByIdPedido(100L);
        assertTrue(guardada.isPresent());
        assertEquals(5000L, guardada.get().getMontoLiquidado().longValue());
        assertNull(guardada.get().getUriPdf());
    }

    @Test
    void ejecutar_idClienteSinFormaPago_lanzaExcepcion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(101L, 999L, 3000L, "Calle 456");

        assertThrows(FormaPagoClienteNoEncontradaException.class, () -> useCase.ejecutar(mensaje));

        final Optional<LiquidacionCliente> guardada = liquidacionRepository.findByIdPedido(101L);
        assertTrue(guardada.isEmpty());
    }

    @Test
    void ejecutar_totalPedidoCero_guardaLiquidacionConMontoCero() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(102L, 1L, 0L, "Calle 789");

        final LiquidacionCliente resultado = useCase.ejecutar(mensaje);

        assertNotNull(resultado);
        assertEquals(0L, resultado.getMontoLiquidado().longValue());
    }

    @Test
    void ejecutar_mensajeConIdPedidoNulo_lanzaExcepcion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(null, 1L, 1000L, "Calle");

        assertThrows(DatosPedidoInvalidosException.class, () -> useCase.ejecutar(mensaje));
    }

    @Test
    void ejecutar_mensajeConIdClienteNulo_lanzaExcepcion() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(103L, null, 1000L, "Calle");

        assertThrows(DatosPedidoInvalidosException.class, () -> useCase.ejecutar(mensaje));
    }
}
