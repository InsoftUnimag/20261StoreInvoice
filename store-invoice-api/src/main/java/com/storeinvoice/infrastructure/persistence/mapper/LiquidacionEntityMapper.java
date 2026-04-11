package com.storeinvoice.infrastructure.persistence.mapper;

import com.storeinvoice.domain.model.LiquidacionCliente;
import com.storeinvoice.infrastructure.persistence.entity.LiquidacionClienteJpaEntity;
import java.util.List;

public final class LiquidacionEntityMapper {

    private LiquidacionEntityMapper() {
    }

    public static LiquidacionCliente toDomain(final LiquidacionClienteJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new LiquidacionCliente(
                entity.getIdLiquidacion(),
                entity.getIdPedido(),
                entity.getIdCliente(),
                entity.getFormaPago(),
                entity.getEstadoLiquidacion(),
                entity.getFechaLiquidacion(),
                entity.getUriPdf(),
                entity.getMontoLiquidado()
        );
    }

    public static List<LiquidacionCliente> toDomainList(final List<LiquidacionClienteJpaEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(LiquidacionEntityMapper::toDomain)
                .toList();
    }
}