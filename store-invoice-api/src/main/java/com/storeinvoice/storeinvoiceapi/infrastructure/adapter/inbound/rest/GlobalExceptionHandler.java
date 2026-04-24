package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ServiceConnectionException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleClienteNotFound(final ClienteNotFoundException ex) {
        LOG.warn("Cliente no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(LiquidacionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleLiquidacionNotFound(final LiquidacionNotFoundException ex) {
        LOG.warn("Liquidación no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(PedidoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePedidoNotFound(final PedidoNotFoundException ex) {
        LOG.warn("Pedido no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.beans.TypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(final org.springframework.beans.TypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Parametros invalidos"));
    }

    @ExceptionHandler(ServiceConnectionException.class)
    public ResponseEntity<Map<String, String>> handleServiceConnection(final ServiceConnectionException ex) {
        LOG.error("Error de conexión con servicio externo: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Servicio temporalmente no disponible. Por favor intente más tarde."));
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(final jakarta.validation.ConstraintViolationException ex) {
        LOG.warn("Validación fallida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Parámetros inválidos: " + ex.getMessage()));
    }
}