package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente;

import com.storeinvoice.storeinvoiceapi.application.dto.messaging.DatosPedidoInventarioMessage;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.domain.exception.DatosPedidoInvalidosException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoClienteNoEncontradaException;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso que recibe los datos de un pedido desde el Modulo de Inventario
 * y registra una liquidacion cliente en estado PENDIENTE.
 */
@Service
@RequiredArgsConstructor
public class RegistrarLiquidacionDesdeInventarioUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrarLiquidacionDesdeInventarioUseCase.class);

    private final LiquidacionRepository liquidacionRepository;
    private final FormaPagoClienteRepository formaPagoClienteRepository;


    @Transactional
    public LiquidacionCliente ejecutar(final DatosPedidoInventarioMessage mensaje) {
        validarMensaje(mensaje);

        LOG.info("Procesando pedido recibido desde inventario. idPedido={}", mensaje.idPedido());

        final FormaPagoCliente formaPago = formaPagoClienteRepository.findByIdCliente(mensaje.idCliente())
                .orElseThrow(() -> new FormaPagoClienteNoEncontradaException(mensaje.idCliente()));

        final LiquidacionCliente liquidacion = construirLiquidacion(mensaje, formaPago);
        final LiquidacionCliente guardada = liquidacionRepository.saveCliente(liquidacion);

        LOG.info("Liquidacion registrada exitosamente. idPedido={}, idLiquidacion={}",
                mensaje.idPedido(), guardada.getIdLiquidacion());

        return guardada;
    }

    private void validarMensaje(final DatosPedidoInventarioMessage mensaje) {
        Optional.ofNullable(mensaje)
                .orElseThrow(() -> new DatosPedidoInvalidosException("mensaje", "El mensaje no puede ser nulo"));

        Optional.ofNullable(mensaje.idPedido())
                .filter(id -> id > 0)
                .orElseThrow(() -> new DatosPedidoInvalidosException("idPedido", "Debe ser mayor a cero"));

        Optional.ofNullable(mensaje.idCliente())
                .filter(id -> id > 0)
                .orElseThrow(() -> new DatosPedidoInvalidosException("idCliente", "Debe ser mayor a cero"));

        Optional.ofNullable(mensaje.totalPedido())
                .filter(total -> total >= 0)
                .orElseThrow(() -> new DatosPedidoInvalidosException("totalPedido", "No puede ser negativo"));
    }

    private LiquidacionCliente construirLiquidacion(
            final DatosPedidoInventarioMessage mensaje,
            final FormaPagoCliente formaPago) {

        final LiquidacionCliente liquidacion = new LiquidacionCliente();
        liquidacion.setIdPedido(mensaje.idPedido());
        liquidacion.setIdCliente(mensaje.idCliente());
        liquidacion.setFormaPago(formaPago.getFormaPago());
        liquidacion.setMontoLiquidado(BigDecimal.valueOf(mensaje.totalPedido()));
        liquidacion.setEstadoLiquidacion(EstadoLiquidacion.PENDIENTE);
        liquidacion.setFechaLiquidacion(LocalDateTime.now());

        return liquidacion;
    }
}

