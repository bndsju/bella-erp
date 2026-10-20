package com.bella.backend.domain.fluxocaixa.model;

/**
 * CONTA_RECEBER e CONTA_PAGAR são alimentadas automaticamente por outros módulos (a referência
 * guarda o id do registro de origem); LANCAMENTO_MANUAL é feito por um operador, sem referência.
 */
public enum OrigemMovimentacaoCaixa {
    CONTA_RECEBER,
    CONTA_PAGAR,
    LANCAMENTO_MANUAL
}
