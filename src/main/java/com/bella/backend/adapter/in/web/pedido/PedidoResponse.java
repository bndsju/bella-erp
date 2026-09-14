package com.bella.backend.adapter.in.web.pedido;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.transportadora.model.Transportadora;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(
        UUID id,
        UUID clienteId,
        String clienteNome,
        UUID orcamentoOrigemId,
        UUID transportadoraId,
        String transportadoraNome,
        List<PedidoItemResponse> itens,
        BigDecimal percentualDesconto,
        BigDecimal valorFrete,
        BigDecimal subtotal,
        BigDecimal valorDesconto,
        BigDecimal valorTotal,
        String condicaoPagamento,
        StatusPedido status,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static PedidoResponse de(Pedido pedido, Cliente cliente, Transportadora transportadora,
                                     List<PedidoItemResponse> itens) {
        return new PedidoResponse(
                pedido.getId(),
                cliente.getId(),
                cliente.getNomeExibicao(),
                pedido.getOrcamentoOrigemId(),
                transportadora == null ? null : transportadora.getId(),
                transportadora == null ? null : transportadora.getNome(),
                itens,
                pedido.getPercentualDesconto(),
                pedido.getValorFrete(),
                pedido.getSubtotal(),
                pedido.getValorDesconto(),
                pedido.getValorTotal(),
                pedido.getCondicaoPagamento(),
                pedido.getStatus(),
                pedido.getCriadoEm(),
                pedido.getAtualizadoEm());
    }
}
