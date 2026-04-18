package com.storeinvoice.store_invoice_api.domain.model;

import java.time.LocalDateTime;
import com.storeinvoice.store_invoice_api.domain.valueobject.FormaPago;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormaPagoCliente {

    private Long idCliente;
    private FormaPago formaPago;
    private LocalDateTime fechaRegistro;
}