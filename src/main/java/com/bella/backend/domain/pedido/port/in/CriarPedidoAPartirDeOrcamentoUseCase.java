package com.bella.backend.domain.pedido.port.in;

import com.bella.backend.domain.pedido.model.Pedido;

import java.util.UUID;

public interface CriarPedidoAPartirDeOrcamentoUseCase {

    /**
     * Copia cliente, itens, desconto, frete e condição de pagamento do orçamento
     * (que precisa estar aprovado) para um novo pedido. A transportadora não existe
     * no orçamento, então é informada aqui — pode ser null e definida depois na edição.
     */
    Pedido criarAPartirDeOrcamento(UUID orcamentoId, UUID transportadoraId);
}
