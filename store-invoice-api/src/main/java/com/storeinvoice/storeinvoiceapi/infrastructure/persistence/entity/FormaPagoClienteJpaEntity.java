package com.storeinvoice.storeinvoiceapi.infrastructure.persistence.entity;

import com.storeinvoice.storeinvoiceapi.domain.model.FormaPago;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "forma_pago_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormaPagoClienteJpaEntity {

    @Id
    @Column(name = "id_cliente")
    private Long idCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pago", nullable = false, length = 50)
    private FormaPago formaPago;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
