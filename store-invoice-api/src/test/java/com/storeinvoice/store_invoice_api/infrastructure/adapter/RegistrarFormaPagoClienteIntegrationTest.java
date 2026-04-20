package com.storeinvoice.store_invoice_api.infrastructure.adapter;

import com.storeinvoice.store_invoice_api.StoreInvoiceApiApplication;
import com.storeinvoice.store_invoice_api.TestcontainersConfiguration;
import com.storeinvoice.store_invoice_api.application.dto.client.ClienteClientResponse;
import com.storeinvoice.store_invoice_api.application.dto.command.RegistrarFormaPagoCommand;
import com.storeinvoice.store_invoice_api.application.dto.response.FormaPagoResponse;
import com.storeinvoice.store_invoice_api.application.service.cliente.RegistrarFormaPagoClienteUseCase;
import com.storeinvoice.store_invoice_api.domain.model.FormaPagoCliente;
import com.storeinvoice.store_invoice_api.infrastructure.adapter.outbound.external.ClienteWebClient;
import com.storeinvoice.store_invoice_api.infrastructure.port.outbound.FormaPagoClienteRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración T015 para el feature "Registrar forma de pago cliente".
 * 
 * Valida:
 * - SC-001: Rendimiento <500ms para guardar en base de datos
 * - SC-002: Disponibilidad inmediata (consultar después de guardar)
 */
@SpringBootTest(classes = StoreInvoiceApiApplication.class)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class RegistrarFormaPagoClienteIntegrationTest {

    @Autowired
    private RegistrarFormaPagoClienteUseCase registrarFormaPagoUseCase;

    @Autowired
    private FormaPagoClienteRepositoryPort formaPagoClienteRepository;

    // Mock del servicio externo para evitar conexión real
    @MockBean
    private ClienteWebClient clienteWebClient;

    private static final Long ID_CLIENTE = 999L;
    private static final String FORMA_PAGO = "CONTRA_ENTREGA";
    private static final Duration MAX_DURATION = Duration.ofMillis(500);

    @BeforeEach
    void setUp() {
        // Configurar mock para devolver cliente válido
        var clienteResponse = new ClienteClientResponse("999", "123", "Test Client", "123", "Test");
        org.mockito.Mockito.when(clienteWebClient.consultarClientePorIdCliente(ID_CLIENTE.toString()))
            .thenReturn(Mono.just(clienteResponse));
    }

    @Test
    void T015_deberiaGuardarFormaPagoConRendimientoMenorA500ms() {
        // Arrange
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(ID_CLIENTE, FORMA_PAGO);

        // Act - Medir tiempo de ejecución
        long startTime = System.nanoTime();
        FormaPagoResponse response = registrarFormaPagoUseCase.registrarFormaPago(command).block();
        long endTime = System.nanoTime();

        // Assert
        assertThat(response).isNotNull();
        
        // Calcular duración en milisegundos
        long durationMs = Duration.ofNanos(endTime - startTime).toMillis();
        
        // Assert - Verificar rendimiento SC-001: <500ms
        assertThat(durationMs)
            .as("SC-001: El tiempo de guardado debe ser menor a 500ms")
            .isLessThan(MAX_DURATION.toMillis());

        System.out.println("Tiempo de guardado: " + durationMs + "ms");
    }

    @Test
    void T015_deberiaEstarDisponibleInmediatamenteDespuesDeGuardar() {
        // Arrange
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(ID_CLIENTE, FORMA_PAGO);

        // Act - Guardar
        registrarFormaPagoUseCase.registrarFormaPago(command).block();

        // Assert - Verificar disponibilidad inmediata SC-002
        var formaPagoGuardada = formaPagoClienteRepository.findByIdCliente(ID_CLIENTE);

        assertThat(formaPagoGuardada)
            .as("SC-002: La forma de pago debe estar disponible inmediatamente después de guardar")
            .isPresent();
        
        FormaPagoCliente fp = formaPagoGuardada.get();
        assertThat(fp.getIdCliente()).isEqualTo(ID_CLIENTE);
        assertThat(fp.getFormaPago().getValue()).isEqualTo(FORMA_PAGO);
    }

    @Test
    void T015_deberiaPersistirFormaPagoCorrectamente() {
        // Arrange
        Long idClienteNuevo = 1000L;
        String formaPago = "CARTERA_COMERCIAL";
        RegistrarFormaPagoCommand command = new RegistrarFormaPagoCommand(idClienteNuevo, formaPago);
        
        // Configurar mock para este cliente
        var clienteResponse = new ClienteClientResponse("1000", "456", "New Client", "456", "New");
        org.mockito.Mockito.when(clienteWebClient.consultarClientePorIdCliente(idClienteNuevo.toString()))
            .thenReturn(Mono.just(clienteResponse));

        // Act
        FormaPagoResponse response = registrarFormaPagoUseCase.registrarFormaPago(command).block();

        // Assert - Verificar persistencia
        var formaPagoGuardada = formaPagoClienteRepository.findByIdCliente(idClienteNuevo);

        assertThat(formaPagoGuardada).isPresent();
        
        FormaPagoCliente fp = formaPagoGuardada.get();
        assertThat(fp.getIdCliente()).isEqualTo(idClienteNuevo);
        assertThat(fp.getFormaPago().getValue()).isEqualTo(formaPago);
        assertThat(fp.getFechaRegistro()).isNotNull();

        // Verificar respuesta
        assertThat(response).isNotNull();
        assertThat(response.idCliente()).isEqualTo(idClienteNuevo);
        assertThat(response.formaPago()).isEqualTo(formaPago);
    }

    @Test
    void T015_deberiaActualizarFormaPagoExistente() {
        // Arrange - Primera forma de pago
        RegistrarFormaPagoCommand command1 = new RegistrarFormaPagoCommand(ID_CLIENTE, "CONTRA_ENTREGA");
        registrarFormaPagoUseCase.registrarFormaPago(command1).block();

        // Act - Actualizar a otra forma de pago
        RegistrarFormaPagoCommand command2 = new RegistrarFormaPagoCommand(ID_CLIENTE, "CARTERA_COMERCIAL");
        registrarFormaPagoUseCase.registrarFormaPago(command2).block();

        // Assert - Verificar que se actualizó
        var formaPagoActualizada = formaPagoClienteRepository.findByIdCliente(ID_CLIENTE);

        assertThat(formaPagoActualizada).isPresent();
        assertThat(formaPagoActualizada.get().getFormaPago().getValue())
            .isEqualTo("CARTERA_COMERCIAL");
    }
}