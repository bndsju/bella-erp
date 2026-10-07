package com.bella.backend.domain.contapagar.model;

/** Recortes de consulta que dependem de data/saldo e, por isso, não são status armazenados. */
public enum SituacaoContaPagar {
    EM_ABERTO,
    PAGAS,
    VENCIDAS
}
