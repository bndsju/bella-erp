package com.bella.backend.domain.contapagar.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ComandoParcelaContaPagar(BigDecimal valor, LocalDate vencimento) {
}
