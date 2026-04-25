package com.storeinvoice.storeinvoiceapi.application.service.pedido;

import com.storeinvoice.storeinvoiceapi.application.port.InventarioServicePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para consultar los productos asociados a un pedido.
 * Orquesta la llamada al puerto de salida y aplica las reglas de negocio:
 * si el pedido no tiene productos, se considera un pedido no encontrado.
 */
@Service
public class ConsultarProductosPedidoUseCase {

    private final InventarioServicePort inventarioServicePort;

    public ConsultarProductosPedidoUseCase(final InventarioServicePort inventarioServicePort) {
        this.inventarioServicePort = inventarioServicePort;
    }

    /**
     * Ejecuta la consulta de productos para un pedido especÃ­fico.
     *
     * @param idPedido ID del pedido. No puede ser nulo ni vacÃ­o.
     * @return Mono con la lista de productos del pedido.
     * @throws IllegalArgumentException si el ID del pedido es nulo o vacÃ­o.
     * @throws PedidoNotFoundException  si el pedido no existe en el mÃ³dulo de
     *                                  inventario.
     */
    public Mono<List<Producto>> ejecutar(final String idPedido) {
        return Mono.justOrEmpty(idPedido)
                .filter(id -> !id.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El ID del pedido no puede ser nulo o vacÃ­o")))
                .flatMap(inventarioServicePort::consultarProductosPorPedido)
                .switchIfEmpty(Mono.error(
                        new PedidoNotFoundException("Pedido no encontrado con el ID proporcionado: " + idPedido)));
    }
}

