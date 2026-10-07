package com.bella.backend.domain.compra.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record ComandoItemCompra(UUID produtoId, BigDecimal quantidade, BigDecimal custoUnitario, BigDecimal desconto) {
}
