package com.bella.backend.domain.orcamento.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record ComandoItemOrcamento(UUID produtoId, BigDecimal quantidade, BigDecimal valorUnitario) {
}
