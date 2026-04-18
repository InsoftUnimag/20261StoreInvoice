package com.storeinvoice.store_invoice_api.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "formas_pago_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormaPagoClienteJpaEntity {

    @Id
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "forma_pago", nullable = false, length = 50)
    private String formaPago;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}