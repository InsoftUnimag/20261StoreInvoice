package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente;

// import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
// import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.messaging.DatosPedidoInventarioMessage;
import com.storeinvoice.storeinvoiceapi.application.port.ClienteServicePort;
import com.storeinvoice.storeinvoiceapi.application.port.InventarioServicePort;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
// import com.storeinvoice.storeinvoiceapi.application.service.pdf.GenerarPdfLiquidacionClienteUseCase;
import com.storeinvoice.storeinvoiceapi.domain.exception.DatosPedidoInvalidosException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoClienteNoEncontradaException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ProductosNoEncontradosException;
import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
// import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcesarPedidoInventarioUseCaseTest {

    @Mock
    private FormaPagoClienteRepository formaPagoClienteRepository;

    @Mock
    private InventarioServicePort inventarioServicePort;

    @Mock
    private ClienteServicePort clienteServicePort;

    // @Mock
    // private GenerarPdfLiquidacionClienteUseCase generarPdfLiquidacionClienteUseCase;

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private ProcesarPedidoInventarioUseCase useCase;

    private FormaPagoCliente formaPagoCliente;
    private List<Producto> productos;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        formaPagoCliente = FormaPagoCliente.builder()
                .idCliente(1L)
                .formaPago(FormaPago.CARTERA_COMERCIAL)
                .fechaRegistro(LocalDateTime.now())
                .build();

        productos = List.of(
                new Producto("1", "Producto A", 2, 100.0, 200.0),
                new Producto("2", "Producto B", 1, 50.0, 50.0)
        );

        cliente = new Cliente("1", "1234567890", "Juan Perez", "3105551234", "Calle 123");

        lenient().doAnswer(invocation -> {
            final java.util.function.Consumer<org.springframework.transaction.TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any(java.util.function.Consumer.class));
    }

    // @Test
    // void ejecutar_flujoExitoso_completaMono() {
    //     final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");
    //     final LiquidacionCliente guardada = new LiquidacionCliente();
    //     guardada.setIdLiquidacion(1L);
    //     guardada.setIdPedido(100L);

    //     when(clienteServicePort.findByIdNacional("1")).thenReturn(Mono.just(cliente));
    //     when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.of(formaPagoCliente));
    //     when(inventarioServicePort.consultarProductosPorPedido("100")).thenReturn(Mono.just(productos));
    //     when(generarPdfLiquidacionClienteUseCase.ejecutar(any(List.class), eq(BigDecimal.valueOf(5000)),
    //             eq(FormaPago.CARTERA_COMERCIAL.name()), any(ClienteLiquidacionDTO.class), eq(100L)))
    //             .thenReturn(Mono.just("file:///tmp/test.pdf"));
    //     when(liquidacionRepository.saveCliente(any(LiquidacionCliente.class))).thenReturn(guardada);

    //     StepVerifier.create(useCase.ejecutar(mensaje))
    //             .verifyComplete();

    //     final ArgumentCaptor<LiquidacionCliente> captor = ArgumentCaptor.forClass(LiquidacionCliente.class);
    //     verify(liquidacionRepository).saveCliente(captor.capture());

    //     final LiquidacionCliente capturada = captor.getValue();
    //     assertEquals(100L, capturada.getIdPedido());
    //     assertEquals(1L, capturada.getIdCliente());
    //     assertEquals(FormaPago.CARTERA_COMERCIAL, capturada.getFormaPago());
    //     assertEquals(EstadoLiquidacion.ENVIADO, capturada.getEstadoLiquidacion());
    //     assertEquals(new BigDecimal("5000"), capturada.getMontoLiquidado());
    //     assertEquals("file:///tmp/test.pdf", capturada.getUriPdf());
    //     assertNotNull(capturada.getFechaLiquidacion());
    // }

    @Test
    void ejecutar_mensajeNulo_completaMonoSinGuardar() {
        StepVerifier.create(useCase.ejecutar(null))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_idPedidoInvalido_completaMonoSinGuardar() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(0L, 1L, 5000L, "Calle 123");

        StepVerifier.create(useCase.ejecutar(mensaje))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_idClienteInvalido_completaMonoSinGuardar() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 0L, 5000L, "Calle 123");

        StepVerifier.create(useCase.ejecutar(mensaje))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_totalPedidoNegativo_completaMonoSinGuardar() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, -1L, "Calle 123");

        StepVerifier.create(useCase.ejecutar(mensaje))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_formaPagoNoEncontrada_completaMonoSinGuardar() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");

        when(clienteServicePort.findByIdNacional("1")).thenReturn(Mono.just(cliente));
        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.empty());

        StepVerifier.create(useCase.ejecutar(mensaje))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_productosVacios_completaMonoSinGuardar() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");

        when(clienteServicePort.findByIdNacional("1")).thenReturn(Mono.just(cliente));
        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.of(formaPagoCliente));
        when(inventarioServicePort.consultarProductosPorPedido("100")).thenReturn(Mono.just(List.of()));

        StepVerifier.create(useCase.ejecutar(mensaje))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_errorConsultaProductos_completaMonoSinGuardar() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");

        when(clienteServicePort.findByIdNacional("1")).thenReturn(Mono.just(cliente));
        when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.of(formaPagoCliente));
        when(inventarioServicePort.consultarProductosPorPedido("100"))
                .thenReturn(Mono.error(new RuntimeException("Connection refused")));

        StepVerifier.create(useCase.ejecutar(mensaje))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_clienteNoEncontrado_completaMonoSinGuardar() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");

        when(clienteServicePort.findByIdNacional("1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.ejecutar(mensaje))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveCliente(any());
    }

    @Test
    void ejecutar_errorConsultaCliente_completaMonoSinGuardar() {
        final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");

        when(clienteServicePort.findByIdNacional("1"))
                .thenReturn(Mono.error(new RuntimeException("Connection refused")));

        StepVerifier.create(useCase.ejecutar(mensaje))
                .verifyComplete();

        verify(liquidacionRepository, never()).saveCliente(any());
    }

    // @Test
    // void ejecutar_errorGeneracionPdf_completaMonoSinGuardar() {
    //     final DatosPedidoInventarioMessage mensaje = new DatosPedidoInventarioMessage(100L, 1L, 5000L, "Calle 123");

    //     when(clienteServicePort.findByIdNacional("1")).thenReturn(Mono.just(cliente));
    //     when(formaPagoClienteRepository.findByIdCliente(1L)).thenReturn(Optional.of(formaPagoCliente));
    //     when(inventarioServicePort.consultarProductosPorPedido("100")).thenReturn(Mono.just(productos));
    //     when(generarPdfLiquidacionClienteUseCase.ejecutar(any(List.class), eq(BigDecimal.valueOf(5000)),
    //             eq(FormaPago.CARTERA_COMERCIAL.name()), any(ClienteLiquidacionDTO.class), eq(100L)))
    //             .thenReturn(Mono.error(new RuntimeException("PDF generation failed")));

    //     StepVerifier.create(useCase.ejecutar(mensaje))
    //             .verifyComplete();

    //     verify(liquidacionRepository, never()).saveCliente(any());
    // }
}

