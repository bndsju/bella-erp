package com.bella.backend.domain.estoque.model;

/**
 * De onde partiu a movimentação: registrada manualmente por um operador, ou disparada
 * automaticamente por outro módulo (Pedidos: reserva, liberação e baixa por entrega;
 * Compras: entrada por recebimento).
 */
public enum OrigemMovimentacaoEstoque {
    MANUAL,
    PEDIDO,
    COMPRA
}
