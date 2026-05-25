package com.storeinvoice.storeinvoiceapi.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcesarEstadoFinalCommand {
    private Long id_pedido;
    private Integer tasa_efectividad;
    private Long id_transportista;
}

