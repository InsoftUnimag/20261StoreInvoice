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
        Objects.requireNonNull(command.getIdPedido(), "id_pedido es requerido");
        Objects.requireNonNull(command.getTasaEfectividad(), "tasa_efectividad es requerida");
        Objects.requireNonNull(command.getIdTransportista(), "id_transportista es requerido");

        final Long idPedido = command.getIdPedido();

        // 2. Verificar idempotencia correcta: solo ignorar si ya fue PROCESADO exitosamente.
        //    Eventos en estado ERROR pueden y deben ser reintentados.
        if (eventoRecibidoRepository.existsByIdPedidoAndEstado(idPedido, EstadoEvento.PROCESADO)) {
            log.warn("El evento final para el pedido {} ya fue procesado exitosamente. Ignorando duplicado.", idPedido);
            return;
        }

        // 3. Registrar el evento como PENDIENTE usando el constructor de creaciÃ³n del dominio
        EventoRecibido evento = new EventoRecibido(
                idPedido,
                command.getTasaEfectividad(),
                command.getIdTransportista()
        );
        evento = eventoRecibidoRepository.save(evento);

        try {
            // 4. Obtener el precio real del pedido desde la tabla 'pedidos'
            //    (recibido del MÃ³dulo de Inventario, no el monto ya liquidado al cliente)
            final BigDecimal precioPedido = pedidoRepository
                    .findPrecioPedidoByIdPedido(idPedido)
                    .orElseThrow(() -> new PedidoNotFoundException(idPedido));

            if (precioPedido.compareTo(BigDecimal.ZERO) <= 0) {
                throw new LiquidacionException(
                        "El precio del pedido no puede ser nulo o cero para liquidar. idPedido=" + idPedido);
            }

            // 5. Delegar validaciÃ³n de rango y cÃ¡lculo al dominio
            //    TasaEfectividad lanza InvalidTasaEfectividadException si fuera de -100..100
            final TasaEfectividad tasa = new TasaEfectividad(command.getTasaEfectividad());
            final BigDecimal montoCalculado = LiquidacionTransportista.calcularMonto(precioPedido, tasa);

            // 6. Reportar pÃ©rdida operativa cuando la tasa es 0 (spec edge case)
            if (tasa.getValor() == 0) {
                log.info(
                        "REPORTE PÃ‰RDIDA OPERATIVA: tasa_efectividad=0 para el pedido {}. "
                        + "El costo del flete no fue cubierto por el transportista.",
                        idPedido);
            }

            // 7. Persistir la liquidaciÃ³n del transportista
            final LiquidacionTransportista liquidacion = new LiquidacionTransportista(
                    null,
                    idPedido,
                    command.getIdTransportista(),
                    montoCalculado,
                    LocalDateTime.now()
            );
            liquidacionRepository.saveTransportista(liquidacion);

            log.info("LiquidaciÃ³n de transportista generada exitosamente para el pedido {}. Monto={}", idPedido, montoCalculado);

            // 8. Marcar el evento como procesado usando comportamiento de dominio
            evento.marcarProcesado();
            eventoRecibidoRepository.save(evento);

        } catch (Exception e) {
            // En caso de error: marcar el evento como ERROR para permitir reintento vÃ­a DLQ
            evento.marcarError();
            eventoRecibidoRepository.save(evento);
            log.error("Error al procesar el estado final para el pedido {}: {}", idPedido, e.getMessage());
            throw e;
        }
    }
}

