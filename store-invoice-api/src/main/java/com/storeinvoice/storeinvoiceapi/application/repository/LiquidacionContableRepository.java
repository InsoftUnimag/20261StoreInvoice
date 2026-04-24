package com.storeinvoice.storeinvoiceapi.application.repository;

import com.storeinvoice.storeinvoiceapi.application.dto.query.ConsultarLiquidacionesContadorQuery;
import com.storeinvoice.storeinvoiceapi.domain.model.LiquidacionContable;
import java.util.List;
import java.util.Optional;

public interface LiquidacionContableRepository {

    List<LiquidacionContable> findAll(ConsultarLiquidacionesContadorQuery query);
}
