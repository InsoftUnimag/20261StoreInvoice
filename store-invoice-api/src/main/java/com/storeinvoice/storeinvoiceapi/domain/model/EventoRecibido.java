package com.storeinvoice.storeinvoiceapi.domain.model;

import java.time.LocalDateTime;
import lombok.Getter;

/**
 * Modelo de dominio que representa un evento de estado final recibido del
 * Módulo de Transporte. Es un objeto de seguimiento/auditoría que registra
 * el ciclo de vida del procesamiento de cada evento.
 *
 * <p>El estado solo puede avanzar mediante métodos de comportamiento explícitos,
 * garantizando invariantes de dominio y evitando mutaciones arbitrarias.
 */
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

    /**
     * Constructor de reconstrucción desde persistencia. Solo debe usarse en mappers.
     */
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

    /**
     * Marca el evento como procesado exitosamente. Registra la fecha de procesamiento.
     */
    public void marcarProcesado() {
        this.estado = EstadoEvento.PROCESADO;
        this.fechaProcesado = LocalDateTime.now();
    }

    /**
     * Marca el evento como fallido. Registra la fecha en que ocurrió el error.
     */
    public void marcarError() {
        this.estado = EstadoEvento.ERROR;
        this.fechaProcesado = LocalDateTime.now();
    }
}
