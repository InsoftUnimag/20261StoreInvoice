package com.storeinvoice.storeinvoiceapi.domain.model;

public record Cliente(
    String idCliente,
    String idNacional,
    String nombre,
    String telefono,
    String direccion
) {
    public Cliente {
        if (idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("ID de cliente es requerido");
        }
        if (idNacional == null || idNacional.isBlank()) {
            throw new IllegalArgumentException("ID nacional es requerido");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre es requerido");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("Teléfono es requerido");
        }
        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("Dirección es requerida");
        }
    }
}