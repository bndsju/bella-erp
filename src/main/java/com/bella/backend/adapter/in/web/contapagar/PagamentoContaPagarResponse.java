package com.bella.backend.adapter.in.web.contapagar;

import com.bella.backend.domain.contapagar.model.PagamentoContaPagar;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PagamentoContaPagarResponse(
        UUID id,
        BigDecimal valorPago,
        LocalDate dataPagamento,
        BigDecimal juros,
        BigDecimal multa,
        BigDecimal desconto,
        BigDecimal valorDesembolsado,
        String observacao,
        Instant criadoEm
) {

    public static PagamentoContaPagarResponse de(PagamentoContaPagar pagamento) {
        return new PagamentoContaPagarResponse(
                pagamento.getId(),
                pagamento.getValorPago(),
                pagamento.getDataPagamento(),
                pagamento.getJuros(),
                pagamento.getMulta(),
                pagamento.getDesconto(),
                pagamento.getValorDesembolsado(),
                pagamento.getObservacao(),
                pagamento.getCriadoEm());
    }
}
