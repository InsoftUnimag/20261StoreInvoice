package com.storeinvoice.storeinvoiceapi.infrastructure.adapter.outbound.persistence;

import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesContadorQuery;
import com.storeinvoice.storeinvoiceapi.application.repository.LiquidacionContableRepository;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionContable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LiquidacionContableRepositoryAdapter implements LiquidacionContableRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<LiquidacionContable> findAll(final ConsultarLiquidacionesContadorQuery query) {
        final String sql = buildFindAllSql(query);
        final Query nativeQuery = entityManager.createNativeQuery(sql);
        applyParameters(nativeQuery, query);
        nativeQuery.setFirstResult(query.offset());
        nativeQuery.setMaxResults(query.tamanoPagina());

        final List<Object[]> results = nativeQuery.getResultList();
        final List<LiquidacionContable> liquidaciones = new ArrayList<>();
        
        results.forEach(row -> liquidaciones.add(mapRowToDomain(row)));
        
        return liquidaciones;
    }

    private String buildFindAllSql(final ConsultarLiquidacionesContadorQuery query) {
        final String baseSql = 
                "SELECT id_liquidacion, id_pedido, 'CLIENTE' as tipo_liquidacion, id_cliente as id_sujeto, monto_liquidado as monto, fecha_liquidacion, uri_pdf as uri_documento " +
                "FROM liquidaciones_cliente " +
                "WHERE 1=1 %s " +
                "UNION ALL " +
                "SELECT id_liquidacion, id_pedido, 'TRANSPORTISTA' as tipo_liquidacion, id_transportista as id_sujeto, monto_calculado as monto, fecha_liquidacion, NULL as uri_documento " +
                "FROM liquidaciones_transportista " +
                "WHERE 1=1 %s ";

        final StringBuilder filtrosCliente = new StringBuilder();
        final StringBuilder filtrosTransportista = new StringBuilder();

        appendTipoFilter(filtrosCliente, filtrosTransportista, query);
        appendIdSujetoFilter(filtrosCliente, filtrosTransportista, query);
        appendFechaDesdeFilter(filtrosCliente, filtrosTransportista, query);
        appendFechaHastaFilter(filtrosCliente, filtrosTransportista, query);

        final String finalSql = String.format(baseSql, filtrosCliente.toString(), filtrosTransportista.toString());
        return finalSql + " ORDER BY fecha_liquidacion DESC";
    }

    private void appendTipoFilter(final StringBuilder cliente, final StringBuilder transportista, final ConsultarLiquidacionesContadorQuery query) {
        Optional.ofNullable(query.tipo()).ifPresent(tipo -> {
            cliente.append(" AND 'CLIENTE' = :tipo");
            transportista.append(" AND 'TRANSPORTISTA' = :tipo");
        });
    }

    private void appendIdSujetoFilter(final StringBuilder cliente, final StringBuilder transportista, final ConsultarLiquidacionesContadorQuery query) {
        Optional.ofNullable(query.idSujeto()).ifPresent(id -> {
            cliente.append(" AND id_cliente = :idSujeto");
            transportista.append(" AND id_transportista = :idSujeto");
        });
    }

    private void appendFechaDesdeFilter(final StringBuilder cliente, final StringBuilder transportista, final ConsultarLiquidacionesContadorQuery query) {
        Optional.ofNullable(query.fechaDesde()).ifPresent(fecha -> {
            cliente.append(" AND fecha_liquidacion >= :fechaDesde");
            transportista.append(" AND fecha_liquidacion >= :fechaDesde");
        });
    }

    private void appendFechaHastaFilter(final StringBuilder cliente, final StringBuilder transportista, final ConsultarLiquidacionesContadorQuery query) {
        Optional.ofNullable(query.fechaHasta()).ifPresent(fecha -> {
            cliente.append(" AND fecha_liquidacion <= :fechaHasta");
            transportista.append(" AND fecha_liquidacion <= :fechaHasta");
        });
    }

    private void applyParameters(final Query nativeQuery, final ConsultarLiquidacionesContadorQuery query) {
        Optional.ofNullable(query.tipo()).ifPresent(tipo -> nativeQuery.setParameter("tipo", tipo));
        Optional.ofNullable(query.idSujeto()).ifPresent(id -> nativeQuery.setParameter("idSujeto", id));
        Optional.ofNullable(query.fechaDesde()).ifPresent(fecha -> nativeQuery.setParameter("fechaDesde", fecha.atStartOfDay()));
        Optional.ofNullable(query.fechaHasta()).ifPresent(fecha -> nativeQuery.setParameter("fechaHasta", fecha.atTime(LocalTime.MAX)));
    }

    private LiquidacionContable mapRowToDomain(final Object[] row) {
        final Long idLiquidacion = ((Number) row[0]).longValue();
        final Long idPedido = ((Number) row[1]).longValue();
        final String tipoLiquidacion = (String) row[2];
        final Long idSujeto = ((Number) row[3]).longValue();
        final BigDecimal monto = (BigDecimal) row[4];
        final Timestamp timestamp = (Timestamp) row[5];
        final String uriDocumento = (String) row[6];

        return new LiquidacionContable(
                idLiquidacion,
                idPedido,
                tipoLiquidacion,
                idSujeto,
                monto,
                timestamp.toLocalDateTime(),
                uriDocumento
        );
    }
}

