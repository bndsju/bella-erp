package com.bella.backend.adapter.in.web.pedido;

import com.bella.backend.domain.pedido.model.PedidoItem;
import com.bella.backend.domain.produto.model.Produto;

import java.math.BigDecimal;
import java.util.UUID;

public record PedidoItemResponse(
        UUID id,
        UUID produtoId,
        String produtoNome,
        String produtoCodigoInterno,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal
) {

    public static PedidoItemResponse de(PedidoItem item, Produto produto) {
        return new PedidoItemResponse(
                item.getId(),
                produto.getId(),
                produto.getNome(),
                produto.getCodigoInterno(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.getSubtotal());
    }
}
