package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.transportista;

import com.storeinvoice.storeinvoiceapi.application.dto.command.ProcesarEstadoFinalCommand;
import com.storeinvoice.storeinvoiceapi.application.repository.EventoRecibidoRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.EstadoFinalInvalidoException;
import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionException;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoEvento;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.EventoRecibido;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionTransportista;
import com.storeinvoice.storeinvoiceapi.domain.valueobject.TasaEfectividad;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

/**
 * Caso de uso que procesa el evento de estado final recibido del Modulo de Transporte.
 *
 * <p>Flujo:
 * <ol>
 *   <li>Validar campos obligatorios del comando</li>
 *   <li>Verificar idempotencia: ignorar si ya fue PROCESADO exitosamente</li>
 *   <li>Registrar el evento como PENDIENTE</li>
 *   <li>Obtener el precio total del pedido desde la liquidacion del cliente</li>
 *   <li>Calcular el monto de liquidacion del transportista usando dominio</li>
 *   <li>Reportar perdida operativa si tasa = 0</li>
 *   <li>Persistir la liquidacion</li>
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
    private final TransactionTemplate transactionTemplate;

    /**
     * Ejecuta el flujo completo de procesamiento de un estado final recibido.
     *
     * @param command datos del estado final recibidos del Modulo de Transporte
     * @return Mono<Void> completado cuando se finaliza el procesamiento
     */
    public Mono<Void> execute(final ProcesarEstadoFinalCommand command) {
        final Long idPedido = command != null ? command.getId_pedido() : null;

        try {
            transactionTemplate.executeWithoutResult(status -> {
                validarComando(command);
                procesarEventoSincrono(command);
            });
            return Mono.empty();
        } catch (Exception e) {
            log.error("Error tecnico procesando estado final idPedido={}: {}", idPedido, e.getMessage(), e);
            return Mono.empty();
        }
    }

    private void validarComando(final ProcesarEstadoFinalCommand command) {
        Optional.ofNullable(command)
                .orElseThrow(() -> new EstadoFinalInvalidoException("command", "El comando no puede ser nulo"));

        Optional.ofNullable(command.getId_pedido())
                .orElseThrow(() -> new EstadoFinalInvalidoException("idPedido", "Es requerido"));

        Optional.ofNullable(command.getTasa_efectividad())
                .orElseThrow(() -> new EstadoFinalInvalidoException("tasaEfectividad", "Es requerida"));

        Optional.ofNullable(command.getId_transportista())
                .orElseThrow(() -> new EstadoFinalInvalidoException("idTransportista", "Es requerido"));
    }

    private void procesarEventoSincrono(final ProcesarEstadoFinalCommand command) {
        final Long idPedido = command.getId_pedido();

        final boolean yaProcesado = eventoRecibidoRepository.existsByIdPedidoAndEstado(
                idPedido, EstadoEvento.PROCESADO);
        if (yaProcesado) {
            return;
        }

        EventoRecibido evento = eventoRecibidoRepository.save(new EventoRecibido(
                idPedido,
                command.getTasa_efectividad(),
                command.getId_transportista()
        ));

        try {
            final BigDecimal precioPedido = liquidacionRepository.findByIdPedido(idPedido)
                    .filter(liquidacion -> liquidacion.getMontoLiquidado().compareTo(BigDecimal.ZERO) > 0)
                    .map(liquidacion -> liquidacion.getMontoLiquidado())
                    .orElseThrow(() -> new LiquidacionException(
                            "No se encontro liquidacion de cliente para el pedido. idPedido=" + idPedido));

            final TasaEfectividad tasa = new TasaEfectividad(command.getTasa_efectividad());
            final BigDecimal montoCalculado = LiquidacionTransportista.calcularMonto(precioPedido, tasa);

            liquidacionRepository.saveTransportista(new LiquidacionTransportista(
                    null,
                    idPedido,
                    command.getId_transportista(),
                    montoCalculado,
                    LocalDateTime.now()
            ));

            final EstadoLiquidacion nuevoEstado = tasa.mapearAEstadoLiquidacion();
            liquidacionRepository.findByIdPedido(idPedido)
                    .ifPresent(liquidacionCliente -> {
                        liquidacionCliente.setEstadoLiquidacion(nuevoEstado);
                        liquidacionRepository.saveCliente(liquidacionCliente);
                    });

            evento.marcarProcesado();
            eventoRecibidoRepository.save(evento);

        } catch (Exception e) {
            evento.marcarError();
            eventoRecibidoRepository.save(evento);
            throw e;
        }
    }
}
