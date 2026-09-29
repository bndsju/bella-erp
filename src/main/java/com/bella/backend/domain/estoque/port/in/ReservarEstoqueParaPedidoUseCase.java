package com.bella.backend.domain.estoque.port.in;

import java.util.List;
import java.util.UUID;

/**
 * Reserva o estoque dos itens de um pedido, chamada pelo fluxo de confirmação de Pedidos.
 * Idempotente: se o pedido já tiver reservas registradas, a chamada não tem efeito.
 */
public interface ReservarEstoqueParaPedidoUseCase {

    void reservarParaPedido(UUID pedidoId, List<ItemMovimentacaoEstoque> itens);
}
