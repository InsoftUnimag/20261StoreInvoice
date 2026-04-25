package com.storeinvoice.storeinvoiceapi.application.service.liquidacion.mapper;

import com.storeinvoice.storeinvoiceapi.application.dto.ClienteLiquidacionDTO;
import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;

/**
 * Mapper que convierte un cliente del dominio del Modulo de Gestion de Clientes
 * a DTO interno para generacion de PDF.
 */
public final class ClienteMapper {

    private ClienteMapper() {
    }

    /**
     * Convierte un cliente de dominio a DTO de liquidacion.
     *
     * @param cliente cliente del Modulo de Gestion de Clientes
     * @return ClienteLiquidacionDTO
     */
    public static ClienteLiquidacionDTO toDto(final Cliente cliente) {
        final Long idCliente;
        try {
            idCliente = Long.parseLong(cliente.idCliente());
        } catch (final NumberFormatException e) {
            throw new ClienteNotFoundException(
                    String.format("El idCliente no es numerico: %s", cliente.idCliente()));
        }

        return new ClienteLiquidacionDTO(
                idCliente,
                cliente.idNacional(),
                cliente.nombre(),
                cliente.telefono(),
                cliente.direccion()
        );
    }
}
