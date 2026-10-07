package com.bella.backend.domain.contapagar.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ComandoPagamentoContaPagar(
        UUID parcelaId,
        BigDecimal valorPago,
        LocalDate dataPagamento,
        BigDecimal juros,
        BigDecimal multa,
        BigDecimal desconto,
        String observacao
) {
}
