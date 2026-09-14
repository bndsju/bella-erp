package com.bella.backend.domain.pedido.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Comando compartilhado entre cadastro e edição manual de pedido. A origem em
 * orçamento não entra aqui — tem seu próprio caso de uso
 * (CriarPedidoAPartirDeOrcamentoUseCase), já que os dados vêm do orçamento, não do usuário.
 */
public record ComandoPedido(
        UUID clienteId,
        UUID transportadoraId,
        List<ComandoItemPedido> itens,
        BigDecimal percentualDesconto,
        BigDecimal valorFrete,
        String condicaoPagamento
) {
}
