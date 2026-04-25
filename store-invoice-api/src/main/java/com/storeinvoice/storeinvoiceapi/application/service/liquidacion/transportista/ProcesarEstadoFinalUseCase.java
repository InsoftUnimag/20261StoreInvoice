package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista;

import com.storeinvoice.storeinvoiceapi.application.dto.command.ProcesarEstadoFinalCommand;
import com.storeinvoice.storeinvoiceapi.application.repository.EventoRecibidoRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.PedidoRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionException;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoEvento;
import com.storeinvoice.storeinvoiceapi.domain.model.EventoRecibido;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import com.storeinvoice.storeinvoiceapi.domain.valueobject.TasaEfectividad;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso que procesa el evento de estado final recibido del MÃ³dulo de Transporte.
 *
 * <p>Flujo:
 * <ol>
 *   <li>Validar campos obligatorios del comando</li>
 *   <li>Verificar idempotencia: ignorar si ya fue PROCESADO exitosamente</li>
 *   <li>Registrar el evento como PENDIENTE</li>
 *   <li>Obtener el precio total del pedido desde la BD (fuente: MÃ³dulo de Inventario)</li>
 *   <li>Calcular el monto de liquidaciÃ³n del transportista usando dominio</li>
 *   <li>Reportar pÃ©rdida operativa si tasa = 0</li>
 *   <li>Persistir la liquidaciÃ³n</li>
 *   <li>Actualizar el estado del evento a PROCESADO</li>
 * </ol>
 *
 * <p>En caso de error, el evento queda en estado ERROR para permitir reintento (DLQ).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcesarEstadoFinalUseCase {

    private final EventoRecibidoRepository eventoRecibidoRepository;
    private final LiquidacionRepository liquidacionRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional
    public void execute(final ProcesarEstadoFinalCommand command) {
        // 1. Validar campos obligatorios del comando (FR-002)
        validarComando(command);

        // 2. Verificar idempotencia correcta y procesar
        Optional.of(command.getIdPedido())
                .filter(id -> !eventoRecibidoRepository.existsByIdPedidoAndEstado(id, EstadoEvento.PROCESADO))
                .ifPresentOrElse(
                        id -> procesarEvento(command),
                        () -> log.warn("El evento final para el pedido {} ya fue procesado exitosamente. Ignorando duplicado.", command.getIdPedido())
                );
    }

    private void validarComando(final ProcesarEstadoFinalCommand command) {
        Optional.ofNullable(command.getIdPedido())
                .orElseThrow(() -> new NullPointerException("id_pedido es requerido"));
        Optional.ofNullable(command.getTasaEfectividad())
                .orElseThrow(() -> new NullPointerException("tasa_efectividad es requerida"));
        Optional.ofNullable(command.getIdTransportista())
                .orElseThrow(() -> new NullPointerException("id_transportista es requerido"));
    }

    private void procesarEvento(final ProcesarEstadoFinalCommand command) {
        final Long idPedido = command.getIdPedido();

        // 3. Registrar el evento como PENDIENTE usando el constructor de creación del dominio
        EventoRecibido evento = eventoRecibidoRepository.save(new EventoRecibido(
                idPedido,
                command.getTasaEfectividad(),
                command.getIdTransportista()
        ));

        try {
            // 4. Obtener el precio real del pedido desde la tabla 'pedidos'
            final BigDecimal precioPedido = pedidoRepository
                    .findPrecioPedidoByIdPedido(idPedido)
                    .filter(precio -> precio.compareTo(BigDecimal.ZERO) > 0)
                    .orElseThrow(() -> new LiquidacionException(
                            "El precio del pedido no puede ser nulo o cero para liquidar. idPedido=" + idPedido));

            // 5. Delegar validación de rango y cálculo al dominio
            final TasaEfectividad tasa = new TasaEfectividad(command.getTasaEfectividad());
            final BigDecimal montoCalculado = LiquidacionTransportista.calcularMonto(precioPedido, tasa);

            // 6. Reportar pérdida operativa cuando la tasa es 0 (spec edge case)
            Optional.of(tasa.getValor())
                    .filter(valor -> valor == 0)
                    .ifPresent(valor -> log.info(
                            "REPORTE PÉRDIDA OPERATIVA: tasa_efectividad=0 para el pedido {}. "
                            + "El costo del flete no fue cubierto por el transportista.",
                            idPedido));

            // 7. Persistir la liquidación del transportista
            liquidacionRepository.saveTransportista(new LiquidacionTransportista(
                    null,
                    idPedido,
                    command.getIdTransportista(),
                    montoCalculado,
                    LocalDateTime.now()
            ));

            log.info("Liquidación de transportista generada exitosamente para el pedido {}. Monto={}", idPedido, montoCalculado);

            // 8. Marcar el evento como procesado usando comportamiento de dominio
            evento.marcarProcesado();
            eventoRecibidoRepository.save(evento);

        } catch (Exception e) {
            // En caso de error: marcar el evento como ERROR para permitir reintento vía DLQ
            evento.marcarError();
            eventoRecibidoRepository.save(evento);
            log.error("Error al procesar el estado final para el pedido {}: {}", idPedido, e.getMessage());
            throw e;
        }
    }
}

