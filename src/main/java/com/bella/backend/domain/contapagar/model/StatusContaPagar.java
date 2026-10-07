package com.bella.backend.domain.contapagar.model;

/**
 * VENCIDA não é um status armazenado: é calculada a partir do vencimento das parcelas e da
 * existência de saldo pendente (ver {@link ContaPagar#estaVencida}).
 */
public enum StatusContaPagar {
    PENDENTE,
    PARCIALMENTE_PAGA,
    PAGA,
    CANCELADA
}
