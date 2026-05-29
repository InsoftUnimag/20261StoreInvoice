package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.response.ErrorResponse;
import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoAlreadyExistsException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.InvalidClientIdException;
import com.storeinvoice.storeinvoiceapi.domain.exception.InvalidFormaPagoException;
import com.storeinvoice.storeinvoiceapi.domain.exception.InvalidTasaEfectividadException;
import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionException;
import com.storeinvoice.storeinvoiceapi.domain.exception.LiquidacionNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.PedidoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ServiceConnectionException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.FormaPagoAlreadyExistsException;
import com.storeinvoice.storeinvoiceapi.domain.exception.InvalidFormaPagoException;
import com.storeinvoice.storeinvoiceapi.domain.exception.DatosPdfInvalidosException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorGeneracionPdfException;
import com.storeinvoice.storeinvoiceapi.domain.exception.ErrorSubidaPdfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

/**
 * Manejador global de excepciones para todos los controladores REST.
 * Convierte excepciones de dominio en respuestas HTTP estructuradas y consistentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClienteNotFound(final ClienteNotFoundException ex,
            final ServerWebExchange exchange) {
        LOG.warn("Cliente no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "Cliente no encontrado",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(InvalidClientIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClientId(final InvalidClientIdException ex,
            final ServerWebExchange exchange) {
        LOG.error("ID de cliente invalido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "ID de cliente invalido",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(LiquidacionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLiquidacionNotFound(final LiquidacionNotFoundException ex,
            final ServerWebExchange exchange) {
        LOG.warn("Liquidacion no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "Liquidacion no encontrada",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(LiquidacionException.class)
    public ResponseEntity<ErrorResponse> handleLiquidacionException(final LiquidacionException ex,
            final ServerWebExchange exchange) {
        LOG.error("Error al generar liquidacion: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "No se puede generar la liquidacion: " + ex.getMessage(),
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(PedidoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePedidoNotFound(final PedidoNotFoundException ex,
            final ServerWebExchange exchange) {
        LOG.warn("Pedido no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "Pedido no encontrado",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(InvalidTasaEfectividadException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTasaEfectividad(final InvalidTasaEfectividadException ex,
            final ServerWebExchange exchange) {
        LOG.warn("Tasa de efectividad invalida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Dato de efectividad invalido. La tasa debe estar entre -100 y 100",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(ServiceConnectionException.class)
    public ResponseEntity<ErrorResponse> handleServiceConnection(final ServiceConnectionException ex,
            final ServerWebExchange exchange) {
        LOG.error("Error de conexion con servicio externo: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Servicio temporalmente no disponible. Por favor intente mas tarde.",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(FormaPagoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFormaPagoNotFound(final FormaPagoNotFoundException ex,
            final ServerWebExchange exchange) {
        LOG.warn("Forma de pago no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "El cliente no tiene forma de pago registrada",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(FormaPagoAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleFormaPagoAlreadyExists(final FormaPagoAlreadyExistsException ex,
            final ServerWebExchange exchange) {
        LOG.warn("Forma de pago ya existe: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "El cliente ya tiene forma de pago registrada",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(InvalidFormaPagoException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormaPago(final InvalidFormaPagoException ex,
            final ServerWebExchange exchange) {
        LOG.warn("Forma de pago invalida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Forma de pago invalida. Valores validos: CONTRA_ENTREGA, CARTERA_COMERCIAL",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(DatosPdfInvalidosException.class)
    public ResponseEntity<ErrorResponse> handleDatosPdfInvalidos(final DatosPdfInvalidosException ex,
            final ServerWebExchange exchange) {
        LOG.warn("Datos PDF invalidos: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "Datos para generar PDF invalidos: " + ex.getMessage(),
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(ErrorGeneracionPdfException.class)
    public ResponseEntity<ErrorResponse> handleErrorGeneracionPdf(final ErrorGeneracionPdfException ex,
            final ServerWebExchange exchange) {
        LOG.error("Error al generar PDF: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error interno al generar el PDF de liquidacion",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(ErrorSubidaPdfException.class)
    public ResponseEntity<ErrorResponse> handleErrorSubidaPdf(final ErrorSubidaPdfException ex,
            final ServerWebExchange exchange) {
        LOG.error("Error al subir PDF: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Error al almacenar el PDF de liquidacion. Por favor intente mas tarde.",
                        exchange.getRequest().getPath().value()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(final Exception ex,
            final ServerWebExchange exchange) {
        LOG.error("Error interno no esperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Ocurrio un error interno en el servidor. Por favor, contacte a soporte si el problema persiste.",
                        exchange.getRequest().getPath().value()
                ));
    }
}

