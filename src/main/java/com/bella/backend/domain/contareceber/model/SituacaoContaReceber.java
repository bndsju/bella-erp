package com.bella.backend.domain.contareceber.model;

/** Recortes de consulta que dependem de data/saldo e, por isso, não são status armazenados. */
public enum SituacaoContaReceber {
    EM_ABERTO,
    RECEBIDAS,
    VENCIDAS
}
