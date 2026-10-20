package com.bella.backend.adapter.in.web.fluxocaixa;

import com.bella.backend.domain.fluxocaixa.model.ResumoCaixa;

import java.math.BigDecimal;

public record ResumoCaixaResponse(
        BigDecimal entradasPrevistas,
        BigDecimal saidasPrevistas,
        BigDecimal entradasRealizadas,
        BigDecimal saidasRealizadas,
        BigDecimal saldoRealizado,
        BigDecimal saldoProjetado
) {

    public static ResumoCaixaResponse de(ResumoCaixa resumo) {
        return new ResumoCaixaResponse(
                resumo.entradasPrevistas(),
                resumo.saidasPrevistas(),
                resumo.entradasRealizadas(),
                resumo.saidasRealizadas(),
                resumo.saldoRealizado(),
                resumo.saldoProjetado());
    }
}
