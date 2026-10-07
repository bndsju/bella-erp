package com.bella.backend.adapter.in.web.compra;

import com.bella.backend.domain.compra.model.CompraItem;
import com.bella.backend.domain.produto.model.Produto;

import java.math.BigDecimal;
import java.util.UUID;

public record CompraItemResponse(
        UUID id,
        UUID produtoId,
        String produtoNome,
        String produtoCodigoInterno,
        BigDecimal quantidade,
        BigDecimal custoUnitario,
        BigDecimal desconto,
        BigDecimal valorTotal
) {

    public static CompraItemResponse de(CompraItem item, Produto produto) {
        return new CompraItemResponse(
                item.getId(),
                produto.getId(),
                produto.getNome(),
                produto.getCodigoInterno(),
                item.getQuantidade(),
                item.getCustoUnitario(),
                item.getDesconto(),
                item.getValorTotal());
    }
}
