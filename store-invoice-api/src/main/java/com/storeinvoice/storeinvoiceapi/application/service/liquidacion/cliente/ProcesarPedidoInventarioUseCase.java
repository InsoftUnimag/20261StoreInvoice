package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.cliente;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import com.storeinvoice.storeinvoiceapi.application.dto.messaging.DatosPedidoInventarioMessage;
import com.storeinvoice.storeinvoiceapi.application.port.ClienteServicePort;
import com.storeinvoice.storeinvoiceapi.application.port.InventarioServicePort;
import com.storeinvoice.storeinvoiceapi.application.repository.FormaPagoClienteRepository;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionRepository;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.mapper.ClienteMapper;
import com.storeinvoice.storeinvoiceapi.application.service.liquidacion.mapper.ProductoMapper;
import com.storeinvoice.storeinvoiceapi.application.service.pdf.GenerarPdfLiquidacionClienteUseCase;
import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.DatosPedidoInvalidosException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorConsultaClienteException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorConsultaProductosException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoClienteNoEncontradaException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ProductosNoEncontradosException;
import com.storeinvoice.storeinvoiceapi.domain.model.EstadoLiquidacion;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Caso de uso orquestador que procesa un pedido recibido desde el Modulo de Inventario.
 * Coordina las consultas a servicios externos (Inventario, Clientes), genera el PDF
 * de liquidacion y guarda el registro con la URI del PDF.
 */
@Service
@RequiredArgsConstructor
public class ProcesarPedidoInventarioUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(ProcesarPedidoInventarioUseCase.class);

    private final FormaPagoClienteRepository formaPagoClienteRepository;
    private final InventarioServicePort inventarioServicePort;
    private final ClienteServicePort clienteServicePort;
    private final GenerarPdfLiquidacionClienteUseCase generarPdfLiquidacionClienteUseCase;
    private final LiquidacionRepository liquidacionRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * Ejecuta el flujo completo de procesamiento de un pedido recibido.
     *
     * @param mensaje datos del pedido recibidos del Modulo de Inventario
     * @return Mono<Void> completado cuando se finaliza el procesamiento
     */
    public Mono<Void> ejecutar(final DatosPedidoInventarioMessage mensaje) {
        final Long idPedido = mensaje != null ? mensaje.idPedido() : null;
        return Mono.fromCallable(() -> {
                    validarMensaje(mensaje);
                    return mensaje;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::consultarFormaPago)
                .flatMap(this::consultarProductosYCliente)
                .flatMap(this::generarPdf)
                .flatMap(this::guardarLiquidacion)
                .doOnSuccess(v -> LOG.info("Liquidacion procesada exitosamente. idPedido={}", idPedido))
                .onErrorResume(e -> {
                    LOG.error("Error procesando pedido idPedido={}: {}", idPedido, e.getMessage());
                    return Mono.empty();
                })
                .then();
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

    private Mono<ContextoProcesamiento> consultarFormaPago(final DatosPedidoInventarioMessage mensaje) {
        return Mono.justOrEmpty(formaPagoClienteRepository.findByIdCliente(mensaje.idCliente()))
                .switchIfEmpty(Mono.error(new FormaPagoClienteNoEncontradaException(mensaje.idCliente())))
                .map(formaPagoCliente -> new ContextoProcesamiento(mensaje, formaPagoCliente.getFormaPago(), null, null));
    }

    private Mono<ContextoProcesamiento> consultarProductosYCliente(final ContextoProcesamiento ctx) {
        return inventarioServicePort.consultarProductosPorPedido(String.valueOf(ctx.mensaje().idPedido()))
                .onErrorMap(e -> new ErrorConsultaProductosException(ctx.mensaje().idPedido(), e))
                .flatMap(productos -> Optional.ofNullable(productos)
                        .filter(p -> !p.isEmpty())
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new ProductosNoEncontradosException(ctx.mensaje().idPedido()))))
                .flatMap(productos -> clienteServicePort.findById(String.valueOf(ctx.mensaje().idCliente()))
                        .onErrorMap(e -> new ErrorConsultaClienteException(ctx.mensaje().idCliente(), e))
                        .switchIfEmpty(Mono.error(new ClienteNotFoundException(ctx.mensaje().idCliente())))
                        .map(cliente -> {
                            final List<ProductoPedidoDTO> productosDto = ProductoMapper.toDtoList(productos);
                            final ClienteLiquidacionDTO clienteDto = ClienteMapper.toDto(cliente);
                            return new ContextoProcesamiento(ctx.mensaje(), ctx.formaPago(), productosDto, clienteDto);
                        })
                );
    }

    private Mono<ContextoConUri> generarPdf(final ContextoProcesamiento ctx) {
        return generarPdfLiquidacionClienteUseCase.ejecutar(
                ctx.productosDto(),
                BigDecimal.valueOf(ctx.mensaje().totalPedido()),
                ctx.formaPago().name(),
                ctx.clienteDto(),
                ctx.mensaje().idPedido()
        ).map(uri -> new ContextoConUri(ctx, uri));
    }

    private Mono<LiquidacionCliente> guardarLiquidacion(final ContextoConUri ctxUri) {
        final LiquidacionCliente liquidacion = construirLiquidacion(
                ctxUri.ctx().mensaje(),
                ctxUri.ctx().formaPago(),
                ctxUri.uri()
        );
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> liquidacionRepository.saveCliente(liquidacion)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private LiquidacionCliente construirLiquidacion(
            final DatosPedidoInventarioMessage mensaje,
            final FormaPago formaPago,
            final String uriPdf) {

        final LiquidacionCliente liquidacion = new LiquidacionCliente();
        liquidacion.setIdPedido(mensaje.idPedido());
        liquidacion.setIdCliente(mensaje.idCliente());
        liquidacion.setFormaPago(formaPago);
        liquidacion.setMontoLiquidado(BigDecimal.valueOf(mensaje.totalPedido()));
        liquidacion.setEstadoLiquidacion(EstadoLiquidacion.PENDIENTE);
        liquidacion.setFechaLiquidacion(LocalDateTime.now());
        liquidacion.setUriPdf(uriPdf);

        return liquidacion;
    }

    private record ContextoProcesamiento(
            DatosPedidoInventarioMessage mensaje,
            FormaPago formaPago,
            List<ProductoPedidoDTO> productosDto,
            ClienteLiquidacionDTO clienteDto) {
    }

    private record ContextoConUri(ContextoProcesamiento ctx, String uri) {
    }
}

