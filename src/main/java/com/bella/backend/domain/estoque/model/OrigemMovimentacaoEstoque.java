package com.bella.backend.domain.estoque.model;

/**
 * De onde partiu a movimentação: registrada manualmente por um operador, ou disparada
 * automaticamente pelo fluxo de Pedidos (reserva, liberação e baixa por entrega).
 */
public enum OrigemMovimentacaoEstoque {
    MANUAL,
    PEDIDO
}
