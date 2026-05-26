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
import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;
import com.storeinvoice.storeinvoiceapi.domain.model.FormaPagoCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionCliente;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
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

@Service
@RequiredArgsConstructor
public class ProcesarPedidoInventarioUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(ProcesarPedidoInventarioUseCase.class);
    private static final long TIMEOUT_SECONDS = 10;

    private final FormaPagoClienteRepository formaPagoClienteRepository;
    private final InventarioServicePort inventarioServicePort;
    private final ClienteServicePort clienteServicePort;
    private final GenerarPdfLiquidacionClienteUseCase generarPdfLiquidacionClienteUseCase;
    private final LiquidacionRepository liquidacionRepository;
    private final TransactionTemplate transactionTemplate;

    public Mono<Void> ejecutar(final DatosPedidoInventarioMessage mensaje) {
        final Long idPedido = mensaje != null ? mensaje.idPedido() : null;
        LOG.debug("Iniciando procesamiento de pedido. idPedido={}", idPedido);

        try {
            transactionTemplate.executeWithoutResult(status -> {
                procesarPedidoSincrono(mensaje);
            });
            LOG.info("Liquidacion procesada exitosamente. idPedido={}", idPedido);
            return Mono.empty();
        } catch (Exception e) {
            LOG.error("Error procesando pedido idPedido={}: {}", idPedido, e.getMessage(), e);
            return Mono.empty();
        }
    }

    private void procesarPedidoSincrono(final DatosPedidoInventarioMessage mensaje) {
        LOG.debug("Paso 1: Validando mensaje. idPedido={}", mensaje.idPedido());
        validarMensaje(mensaje);

        LOG.debug("Paso 2: Resolviendo cliente por idNacional={}", mensaje.idCliente());
        final Cliente cliente = resolverCliente(mensaje);
        LOG.debug("Paso 3: Cliente resuelto. idNacional={} -> idClienteBD={}", mensaje.idCliente(), cliente.idCliente());

        LOG.debug("Paso 4: Consultando forma de pago para idClienteBD={}", cliente.idCliente());
        final FormaPago formaPago = consultarFormaPago(cliente.idCliente());
        LOG.debug("Paso 5: Forma de pago consultada. formaPago={}", formaPago);

        LOG.debug("Paso 6: Consultando productos para idPedido={}", mensaje.idPedido());
        final List<Producto> productos = consultarProductos(mensaje.idPedido());
        LOG.debug("Paso 7: Productos consultados. cantidad={}", productos.size());

        final List<ProductoPedidoDTO> productosDto = ProductoMapper.toDtoList(productos);
        final ClienteLiquidacionDTO clienteDto = ClienteMapper.toDto(cliente);

        LOG.debug("Paso 8: Generando PDF para idPedido={}", mensaje.idPedido());
        final String uriPdf = generarPdf(productosDto, mensaje.totalPedido(), formaPago.name(), clienteDto, mensaje.idPedido());
        LOG.debug("Paso 9: PDF generado. uriPdf={}", uriPdf);

        LOG.debug("Paso 10: Guardando liquidacion en BD");
        guardarLiquidacion(mensaje, cliente, formaPago, uriPdf);
        LOG.debug("Paso 11: Liquidacion guardada exitosamente");
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

    private Cliente resolverCliente(final DatosPedidoInventarioMessage mensaje) {
        try {
            return clienteServicePort.findByIdNacional(String.valueOf(mensaje.idCliente()))
                    .timeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();
        } catch (Exception e) {
            throw new ErrorConsultaClienteException(mensaje.idCliente(), e);
        }
    }

    private FormaPago consultarFormaPago(final String idCliente) {
        final Long idClienteLong = Long.valueOf(idCliente);
        return formaPagoClienteRepository.findByIdCliente(idClienteLong)
                .map(FormaPagoCliente::getFormaPago)
                .orElseThrow(() -> new FormaPagoClienteNoEncontradaException(idClienteLong));
    }

    private List<Producto> consultarProductos(final Long idPedido) {
        try {
            return inventarioServicePort.consultarProductosPorPedido(String.valueOf(idPedido))
                    .timeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();
        } catch (Exception e) {
            throw new ErrorConsultaProductosException(idPedido, e);
        }
    }

    private String generarPdf(final List<ProductoPedidoDTO> productosDto, final Long totalPedido,
            final String formaPago, final ClienteLiquidacionDTO clienteDto, final Long idPedido) {
        try {
            return generarPdfLiquidacionClienteUseCase.ejecutar(
                    productosDto,
                    BigDecimal.valueOf(totalPedido),
                    formaPago,
                    clienteDto,
                    idPedido
            ).timeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS)).block();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    private void guardarLiquidacion(final DatosPedidoInventarioMessage mensaje,
            final Cliente cliente, final FormaPago formaPago, final String uriPdf) {
        final Long idClienteReal = Long.valueOf(cliente.idCliente());
        final LiquidacionCliente liquidacion = new LiquidacionCliente();
        liquidacion.setIdPedido(mensaje.idPedido());
        liquidacion.setIdCliente(idClienteReal);
        liquidacion.setFormaPago(formaPago);
        liquidacion.setMontoLiquidado(BigDecimal.valueOf(mensaje.totalPedido()));
        liquidacion.setEstadoLiquidacion(EstadoLiquidacion.ENVIADO);
        liquidacion.setFechaLiquidacion(LocalDateTime.now());
        liquidacion.setUriPdf(uriPdf);

        liquidacionRepository.saveCliente(liquidacion);
    }
}