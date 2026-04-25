package com.storeinvoice.storeinvoiceapi.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormaPagoCliente {

    private Long idCliente;
    private FormaPago formaPago;
    private LocalDateTime fechaRegistro;

    /**
     * Valida que los campos obligatorios no sean nulos.
     * @throws IllegalArgumentException si idCliente o formaPago son nulos
     */
    public void validar() {
        if (idCliente == null) {
            throw new IllegalArgumentException("ID de cliente es requerido");
        }
        if (formaPago == null) {
            throw new IllegalArgumentException("Forma de pago es requerida");
        }
    }
}

