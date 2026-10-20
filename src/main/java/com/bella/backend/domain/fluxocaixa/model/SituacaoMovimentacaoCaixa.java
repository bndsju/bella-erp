package com.bella.backend.domain.fluxocaixa.model;

/**
 * PREVISTO é projeção (ainda não ocorreu); REALIZADO é movimentação efetiva. Uma mesma operação
 * financeira é um único registro que passa de PREVISTO para REALIZADO, nunca dois registros —
 * por isso a projeção não é somada ao realizado em duplicidade.
 */
public enum SituacaoMovimentacaoCaixa {
    PREVISTO,
    REALIZADO
}
