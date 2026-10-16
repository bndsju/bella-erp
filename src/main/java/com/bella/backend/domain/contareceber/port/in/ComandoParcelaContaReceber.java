package com.bella.backend.domain.contareceber.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ComandoParcelaContaReceber(BigDecimal valor, LocalDate vencimento) {
}
