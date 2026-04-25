package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.external;

import com.storeinvoice.storeinvoiceapi.application.port.ClienteServicePort;
import com.storeinvoice.storeinvoiceapi.domain.exception.ClienteNotFoundException;
import com.storeinvoice.storeinvoiceapi.domain.exception.InvalidClientIdException;
import com.storeinvoice.storeinvoiceapi.domain.model.Cliente;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
@Profile("!prod")
public class ClienteMockAdapter implements ClienteServicePort {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");

    @Override
    public Mono<Cliente> findByIdNacional(final String idNacional) {
        return Optional.ofNullable(idNacional)
                .filter(id -> !id.isBlank())
                .filter(id -> NUMERIC_PATTERN.matcher(id).matches())
                .map(id -> new Cliente("100", id, "Juan Perez Mock", "3001234567", "Calle 123 #45-67"))
                .map(Mono::just)
                .orElseGet(() -> Mono.error(new InvalidClientIdException("ID de cliente invalido")));
    }

    @Override
    public Mono<Cliente> findById(final String idCliente) {
        return Optional.<String>of(idCliente)
                .filter(id -> !id.isBlank())
                .filter(id -> NUMERIC_PATTERN.matcher(id).matches())
                .filter("2"::equals)
                .map(id -> new Cliente(id, "12345678", "Juan Perez Mock", "3001234567", "Calle 123 #45-67"))
                .map(Mono::just)
                .orElseGet(() -> Mono.error(
                        Optional.<String>of(idCliente)
                                .filter(id -> !id.isBlank())
                                .filter(id -> NUMERIC_PATTERN.matcher(id).matches())
                                .map(id -> (Exception) new ClienteNotFoundException("Cliente no encontrado con el ID proporcionado"))
                                .orElseGet(() -> new InvalidClientIdException("ID de cliente invalido"))));
    }
}
