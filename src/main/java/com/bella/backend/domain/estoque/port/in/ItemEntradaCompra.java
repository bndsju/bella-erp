package com.bella.backend.domain.estoque.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemEntradaCompra(UUID produtoId, BigDecimal quantidade, BigDecimal custoUnitario) {
}
