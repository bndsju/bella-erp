package com.bella.backend.adapter.in.web.contapagar;

import com.bella.backend.domain.contapagar.port.in.ComandoPagamentoContaPagar;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PagamentoContaPagarRequest(
        @NotNull(message = "Parcela é obrigatória") UUID parcelaId,
        @NotNull(message = "Valor pago é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor pago deve ser maior que zero") BigDecimal valorPago,
        LocalDate dataPagamento,
        @DecimalMin(value = "0", message = "Juros não podem ser negativos") BigDecimal juros,
        @DecimalMin(value = "0", message = "Multa não pode ser negativa") BigDecimal multa,
        @DecimalMin(value = "0", message = "Desconto não pode ser negativo") BigDecimal desconto,
        String observacao
) {

    public ComandoPagamentoContaPagar paraComando() {
        return new ComandoPagamentoContaPagar(parcelaId, valorPago, dataPagamento, juros, multa, desconto,
                observacao);
    }
}
