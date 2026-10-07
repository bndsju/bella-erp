package com.bella.backend.adapter.in.web.estoque;

import com.bella.backend.domain.estoque.model.PosicaoEstoqueProduto;
import com.bella.backend.domain.produto.model.Produto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaldoEstoqueResponse(
        UUID produtoId,
        String produtoNome,
        String produtoCodigoInterno,
        BigDecimal estoqueFisico,
        BigDecimal quantidadeReservada,
        BigDecimal quantidadeDisponivel,
        BigDecimal estoqueMinimo
) {

    public static SaldoEstoqueResponse de(PosicaoEstoqueProduto posicao, Produto produto) {
        return new SaldoEstoqueResponse(
                produto.getId(),
                produto.getNome(),
                produto.getCodigoInterno(),
                posicao.estoqueFisico(),
                posicao.quantidadeReservada(),
                posicao.quantidadeDisponivel(),
                posicao.estoqueMinimo());
    }
}
