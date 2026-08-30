package com.bella.backend.adapter.in.web.orcamento;

import com.bella.backend.domain.orcamento.model.OrcamentoItem;
import com.bella.backend.domain.produto.model.Produto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrcamentoItemResponse(
        UUID id,
        UUID produtoId,
        String produtoNome,
        String produtoCodigoInterno,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal
) {

    public static OrcamentoItemResponse de(OrcamentoItem item, Produto produto) {
        return new OrcamentoItemResponse(
                item.getId(),
                produto.getId(),
                produto.getNome(),
                produto.getCodigoInterno(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.getSubtotal());
    }
}
