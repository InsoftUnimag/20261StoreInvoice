package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.inbound.rest;

import com.storeinvoice.storeinvoiceapi.application.dto.response.TotalPedidoResponse;
import com.storeinvoice.storeinvoiceapi.application.service.pedido.ConsultarTotalPedidoUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pedidos")
@Validated
@Tag(name = "Pedidos", description = "Operaciones relacionadas con pedidos")
public class PedidoController {

    private final ConsultarTotalPedidoUseCase consultarTotalPedidoUseCase;

    public PedidoController(final ConsultarTotalPedidoUseCase consultarTotalPedidoUseCase) {
        this.consultarTotalPedidoUseCase = consultarTotalPedidoUseCase;
    }

    @GetMapping("/{id_pedido}/total")
    public ResponseEntity<TotalPedidoResponse> consultarTotal(
            @Parameter(description = "ID unico del pedido", required = true, example = "1") @PathVariable @NotNull @Min(1) final Long id_pedido) {

        final BigDecimal total = consultarTotalPedidoUseCase.execute(id_pedido);
        final var response = new TotalPedidoResponse(id_pedido, total);

        return ResponseEntity.ok(response);
    }
}
