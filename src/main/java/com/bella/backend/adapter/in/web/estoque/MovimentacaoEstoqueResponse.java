package com.bella.backend.adapter.in.web.estoque;

import com.bella.backend.domain.estoque.model.MovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.OrigemMovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
import com.bella.backend.domain.produto.model.Produto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovimentacaoEstoqueResponse(
        UUID id,
        UUID produtoId,
        String produtoNome,
        String produtoCodigoInterno,
        TipoMovimentacaoEstoque tipo,
        BigDecimal quantidade,
        String motivo,
        String observacao,
        OrigemMovimentacaoEstoque origem,
        UUID origemOperacaoId,
        String usuarioResponsavel,
        Instant dataHora
) {

    public static MovimentacaoEstoqueResponse de(MovimentacaoEstoque movimentacao, Produto produto) {
        return new MovimentacaoEstoqueResponse(
                movimentacao.getId(),
                produto.getId(),
                produto.getNome(),
                produto.getCodigoInterno(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getMotivo(),
                movimentacao.getObservacao(),
                movimentacao.getOrigem(),
                movimentacao.getOrigemOperacaoId(),
                movimentacao.getUsuarioResponsavel(),
                movimentacao.getDataHora());
    }
}
