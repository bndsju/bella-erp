package com.bella.backend.adapter.in.web.contapagar;

import com.bella.backend.domain.contapagar.port.in.ComandoParcelaContaPagar;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelaContaPagarRequest(
        @NotNull(message = "Valor da parcela é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor da parcela deve ser maior que zero") BigDecimal valor,
        @NotNull(message = "Vencimento da parcela é obrigatório") LocalDate vencimento
) {

    public ComandoParcelaContaPagar paraComando() {
        return new ComandoParcelaContaPagar(valor, vencimento);
    }
}
