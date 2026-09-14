package com.bella.backend.adapter.in.web.venda;

import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.venda.model.VendaItem;

import java.math.BigDecimal;
import java.util.UUID;

public record VendaItemResponse(
        UUID id,
        UUID produtoId,
        String produtoNome,
        String produtoCodigoInterno,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal
) {

    public static VendaItemResponse de(VendaItem item, Produto produto) {
        return new VendaItemResponse(
                item.getId(),
                produto.getId(),
                produto.getNome(),
                produto.getCodigoInterno(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.getSubtotal());
    }
}
