package com.bella.backend.domain.contareceber.model;

/**
 * VENCIDA não é um status armazenado: é calculada a partir do vencimento das parcelas e da
 * existência de saldo pendente (ver {@link ContaReceber#estaVencida}).
 */
public enum StatusContaReceber {
    PENDENTE,
    PARCIALMENTE_RECEBIDA,
    RECEBIDA,
    CANCELADA
}
