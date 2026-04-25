package com.storeinvoice.storeinvoiceapi.application.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcesarEstadoFinalCommand {
    @NotNull(message = "El idPedido es requerido")
    private Long idPedido;
    
    @NotNull(message = "La tasaEfectividad es requerida")
    private Integer tasaEfectividad;
    
    @NotNull(message = "El idTransportista es requerido")
    private Long idTransportista;
}
