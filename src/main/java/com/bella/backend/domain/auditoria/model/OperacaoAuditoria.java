package com.bella.backend.domain.auditoria.model;

/**
 * Operações registradas. Pagamentos, recebimentos e movimentações de estoque já têm histórico
 * próprio nos seus módulos; aqui entram apenas como um registro de quem executou e quando.
 */
public enum OperacaoAuditoria {
    CRIACAO,
    ALTERACAO,
    INATIVACAO,
    REATIVACAO,
    CANCELAMENTO,
    ALTERACAO_STATUS,
    REGISTRO_PAGAMENTO,
    MOVIMENTACAO_ESTOQUE
}
