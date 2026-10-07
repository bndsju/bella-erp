package com.bella.backend.domain.estoque.port.in;

import java.util.UUID;

/**
 * Converte a reserva de um pedido em saída definitiva de estoque, chamada quando o pedido
 * chega ao status ENTREGUE. Idempotente: se a baixa já tiver sido processada para o pedido,
 * a chamada não tem efeito.
 */
public interface ConsumirReservaEstoqueParaPedidoUseCase {

    void consumirReservaParaPedido(UUID pedidoId);
}
