package com.bella.backend.adapter.in.web.contareceber;

import com.bella.backend.domain.contareceber.port.in.ComandoParcelaContaReceber;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelaContaReceberRequest(
        @NotNull(message = "Valor da parcela é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor da parcela deve ser maior que zero") BigDecimal valor,
        @NotNull(message = "Vencimento da parcela é obrigatório") LocalDate vencimento
) {

    public ComandoParcelaContaReceber paraComando() {
        return new ComandoParcelaContaReceber(valor, vencimento);
    }
}
