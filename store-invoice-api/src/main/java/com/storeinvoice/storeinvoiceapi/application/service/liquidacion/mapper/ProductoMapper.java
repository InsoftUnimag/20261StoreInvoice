package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.mapper;

import com.storeinvoice.storeinvoiceapi.application.dto.ProductoPedidoDTO;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorConsultaProductosException;
import com.storeinvoice.storeinvoiceapi.domain.model.Producto;
import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper que convierte productos del dominio del Modulo de Inventario
 * a DTOs internos para generacion de PDF.
 */
public final class ProductoMapper {

    private ProductoMapper() {
    }

    /**
     * Convierte una lista de productos de dominio a una lista de DTOs.
     *
     * @param productos lista de productos del Modulo de Inventario
     * @return lista de ProductoPedidoDTO
     */
    public static List<ProductoPedidoDTO> toDtoList(final List<Producto> productos) {
        return productos.stream()
                .map(ProductoMapper::toDto)
                .toList();
    }

    private static ProductoPedidoDTO toDto(final Producto producto) {
        final Long idProducto;
        try {
            idProducto = Long.parseLong(producto.idProducto());
        } catch (final NumberFormatException e) {
            throw new ErrorConsultaProductosException(
                    String.format("El idProducto no es numerico: %s", producto.idProducto()));
        }

        return new ProductoPedidoDTO(
                idProducto,
                producto.nombre(),
                producto.cantidad(),
                BigDecimal.valueOf(producto.precioUnitario()),
                BigDecimal.valueOf(producto.subtotal())
        );
    }
}
