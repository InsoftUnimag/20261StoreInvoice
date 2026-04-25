package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity;

import com.storeinvoice.storeinvoiceapi.domain.model.EstadoEvento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "evento_recibido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoRecibidoJpaEntity {

    @Id
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "tasa_efectividad", nullable = false)
    private int tasaEfectividad;

    @Column(name = "id_transportista", nullable = false)
    private Long idTransportista;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoEvento estado;

    @Column(name = "fecha_recibido", nullable = false)
    private LocalDateTime fechaRecibido;

    @Column(name = "fecha_procesado")
    private LocalDateTime fechaProcesado;
}

