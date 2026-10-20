package com.bella.backend.adapter.in.web.contareceber;

import com.bella.backend.domain.contareceber.model.RecebimentoContaReceber;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RecebimentoContaReceberResponse(
        UUID id,
        BigDecimal valorRecebido,
        LocalDate dataRecebimento,
        BigDecimal juros,
        BigDecimal multa,
        BigDecimal desconto,
        BigDecimal valorTotalRecebido,
        String observacao,
        Instant criadoEm
) {

    public static RecebimentoContaReceberResponse de(RecebimentoContaReceber recebimento) {
        return new RecebimentoContaReceberResponse(
                recebimento.getId(),
                recebimento.getValorRecebido(),
                recebimento.getDataRecebimento(),
                recebimento.getJuros(),
                recebimento.getMulta(),
                recebimento.getDesconto(),
                recebimento.getValorTotalRecebido(),
                recebimento.getObservacao(),
                recebimento.getCriadoEm());
    }
}
