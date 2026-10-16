package com.bella.backend.adapter.in.web.contareceber;

import com.bella.backend.domain.contareceber.model.ParcelaContaReceber;
import com.bella.backend.domain.contareceber.model.StatusParcela;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ParcelaContaReceberResponse(
        UUID id,
        int numero,
        BigDecimal valor,
        LocalDate vencimento,
        StatusParcela status,
        boolean vencida,
        BigDecimal valorRecebido,
        BigDecimal saldoPendente,
        List<RecebimentoContaReceberResponse> recebimentos
) {

    public static ParcelaContaReceberResponse de(ParcelaContaReceber parcela, LocalDate hoje) {
        return new ParcelaContaReceberResponse(
                parcela.getId(),
                parcela.getNumero(),
                parcela.getValor(),
                parcela.getVencimento(),
                parcela.getStatus(),
                parcela.estaVencida(hoje),
                parcela.getValorRecebido(),
                parcela.getSaldoPendente(),
                parcela.getRecebimentos().stream().map(RecebimentoContaReceberResponse::de).toList());
    }
}
