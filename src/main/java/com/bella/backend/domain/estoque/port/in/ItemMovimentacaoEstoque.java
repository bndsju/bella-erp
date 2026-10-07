package com.bella.backend.domain.estoque.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemMovimentacaoEstoque(UUID produtoId, BigDecimal quantidade) {
}
