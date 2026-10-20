package com.bella.backend.adapter.in.web.contapagar;

import com.bella.backend.domain.contapagar.model.ParcelaContaPagar;
import com.bella.backend.domain.contapagar.model.StatusParcela;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ParcelaContaPagarResponse(
        UUID id,
        int numero,
        BigDecimal valor,
        LocalDate vencimento,
        StatusParcela status,
        boolean vencida,
        BigDecimal valorPago,
        BigDecimal saldoPendente,
        List<PagamentoContaPagarResponse> pagamentos
) {

    public static ParcelaContaPagarResponse de(ParcelaContaPagar parcela, LocalDate hoje) {
        return new ParcelaContaPagarResponse(
                parcela.getId(),
                parcela.getNumero(),
                parcela.getValor(),
                parcela.getVencimento(),
                parcela.getStatus(),
                parcela.estaVencida(hoje),
                parcela.getValorPago(),
                parcela.getSaldoPendente(),
                parcela.getPagamentos().stream().map(PagamentoContaPagarResponse::de).toList());
    }
}
