package com.bella.backend.domain.contareceber.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ComandoRecebimentoContaReceber(
        UUID parcelaId,
        BigDecimal valorRecebido,
        LocalDate dataRecebimento,
        BigDecimal juros,
        BigDecimal multa,
        BigDecimal desconto,
        String observacao
) {
}
