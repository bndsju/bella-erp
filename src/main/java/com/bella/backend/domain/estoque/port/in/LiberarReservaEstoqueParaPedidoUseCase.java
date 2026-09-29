package com.bella.backend.domain.estoque.port.in;

import java.util.UUID;

/**
 * Libera as reservas de estoque associadas a um pedido, chamada pelo fluxo de cancelamento de
 * Pedidos. Não tem efeito se o pedido nunca chegou a reservar estoque (cancelado antes de ser
 * confirmado) ou se as reservas já foram liberadas/consumidas — idempotente por construção.
 */
public interface LiberarReservaEstoqueParaPedidoUseCase {

    void liberarReservaParaPedido(UUID pedidoId);
}
