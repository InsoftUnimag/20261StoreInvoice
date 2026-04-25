package com.storeinvoice.storeinvoiceapi.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcesarEstadoFinalCommand {
    private Long idPedido;
    private Integer tasaEfectividad;
    private Long idTransportista;
}

