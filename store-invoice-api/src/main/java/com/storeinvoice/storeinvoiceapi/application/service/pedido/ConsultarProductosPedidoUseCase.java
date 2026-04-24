package com.storeinvoice.storeinvoiceapi.application.service.pedido;

import com.storeinvoice.storeinvoiceapi.application.port.InventarioServicePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para consultar los productos asociados a un pedido.
 * Orquesta la llamada al puerto de salida y aplica las reglas de negocio:
 * si el pedido no tiene productos, se considera un pedido no encontrado.
 */
@Service
public class ConsultarProductosPedidoUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultarProductosPedidoUseCase.class);

    private final InventarioServicePort inventarioServicePort;

    public ConsultarProductosPedidoUseCase(final InventarioServicePort inventarioServicePort) {
        this.inventarioServicePort = inventarioServicePort;
    }

    /**
     * Ejecuta la consulta de productos para un pedido específico.
     *
     * @param idPedido ID del pedido. No puede ser nulo ni vacío.
     * @return Mono con la lista de productos del pedido.
     * @throws IllegalArgumentException  si el ID del pedido es nulo o vacío.
     * @throws PedidoNotFoundException   si el pedido no existe en el módulo de inventario.
     */
    public Mono<List<Producto>> ejecutar(final String idPedido) {
        if (idPedido == null || idPedido.isBlank()) {
            return Mono.error(new IllegalArgumentException("El ID del pedido no puede ser nulo o vacío"));
        }

        LOG.info("Consultando productos para el pedido con ID: {}", idPedido);

        return inventarioServicePort.consultarProductosPorPedido(idPedido)
                .switchIfEmpty(Mono.error(
                        new PedidoNotFoundException("Pedido no encontrado con el ID proporcionado: " + idPedido)));
    }
}
