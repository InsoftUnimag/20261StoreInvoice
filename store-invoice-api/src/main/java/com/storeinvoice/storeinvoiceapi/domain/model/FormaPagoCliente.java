package com.storeinvoice.storeinvoiceapi.domain.model;

import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Long idCliente;

    @NotNull
    private FormaPago formaPago;

    private LocalDateTime fechaRegistro;
}
