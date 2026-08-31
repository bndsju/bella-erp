package com.bella.backend.adapter.in.web.venda;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.venda.model.FormaPagamento;
import com.bella.backend.domain.venda.model.StatusVenda;
import com.bella.backend.domain.venda.model.Venda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VendaResponse(
        UUID id,
        UUID pedidoId,
        UUID clienteId,
        String clienteNome,
        List<VendaItemResponse> itens,
        BigDecimal percentualDesconto,
        BigDecimal valorFrete,
        BigDecimal subtotal,
        BigDecimal valorDesconto,
        BigDecimal valorTotal,
        FormaPagamento formaPagamento,
        String condicaoPagamento,
        StatusVenda status,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static VendaResponse de(Venda venda, Cliente cliente, List<VendaItemResponse> itens) {
        return new VendaResponse(
                venda.getId(),
                venda.getPedidoId(),
                cliente.getId(),
                cliente.getNomeExibicao(),
                itens,
                venda.getPercentualDesconto(),
                venda.getValorFrete(),
                venda.getSubtotal(),
                venda.getValorDesconto(),
                venda.getValorTotal(),
                venda.getFormaPagamento(),
                venda.getCondicaoPagamento(),
                venda.getStatus(),
                venda.getCriadoEm(),
                venda.getAtualizadoEm());
    }
}
