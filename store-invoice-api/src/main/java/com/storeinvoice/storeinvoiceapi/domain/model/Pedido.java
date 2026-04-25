package com.storeinvoice.storeinvoiceapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    private Long idPedido;
    private Long idCliente;
    private BigDecimal precioTotal;
    private LocalDateTime fechaRecibido;
}

