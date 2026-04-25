package com.storeinvoice.storeinvoiceapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo de dominio que representa un pedido registrado en el Sistema Financiero,
 * recibido originalmente desde el Módulo de Inventario.
 * Contiene el precio total del pedido necesario para calcular las liquidaciones.
 */
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
