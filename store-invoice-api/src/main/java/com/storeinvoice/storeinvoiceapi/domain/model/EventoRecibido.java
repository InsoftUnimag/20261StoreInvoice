package com.storeinvoice.storeinvoiceapi.domain.model;

import java.time.LocalDateTime;
import lombok.Getter;


@Getter
public class EventoRecibido {

    private Long idPedido;
    private int tasaEfectividad;
    private Long idTransportista;
    private EstadoEvento estado;
    private LocalDateTime fechaRecibido;
    private LocalDateTime fechaProcesado;

    /**
     * Constructor para crear un nuevo EventoRecibido en estado PENDIENTE.
     * El estado inicial siempre es PENDIENTE; no se puede crear directamente en otro estado.
     */
    public EventoRecibido(
            final Long idPedido,
            final int tasaEfectividad,
            final Long idTransportista) {
        this.idPedido = idPedido;
        this.tasaEfectividad = tasaEfectividad;
        this.idTransportista = idTransportista;
        this.estado = EstadoEvento.PENDIENTE;
        this.fechaRecibido = LocalDateTime.now();
        this.fechaProcesado = null;
    }

    public EventoRecibido(
            final Long idPedido,
            final int tasaEfectividad,
            final Long idTransportista,
            final EstadoEvento estado,
            final LocalDateTime fechaRecibido,
            final LocalDateTime fechaProcesado) {
        this.idPedido = idPedido;
        this.tasaEfectividad = tasaEfectividad;
        this.idTransportista = idTransportista;
        this.estado = estado;
        this.fechaRecibido = fechaRecibido;
        this.fechaProcesado = fechaProcesado;
    }


    public void marcarProcesado() {
        this.estado = EstadoEvento.PROCESADO;
        this.fechaProcesado = LocalDateTime.now();
    }

    public void marcarError() {
        this.estado = EstadoEvento.ERROR;
        this.fechaProcesado = LocalDateTime.now();
    }
}

