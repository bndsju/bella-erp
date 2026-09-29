package com.bella.backend.domain.estoque.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MovimentacaoEstoqueTest {

    private final UUID produtoId = UUID.randomUUID();

    @Test
    void registraMovimentacaoValida() {
        MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrar(
                produtoId, TipoMovimentacaoEstoque.ENTRADA, new BigDecimal("5"), "Compra de fornecedor", "nf 123",
                OrigemMovimentacaoEstoque.MANUAL, null, "operador@bella.com");

        assertThat(movimentacao.getId()).isNotNull();
        assertThat(movimentacao.getProdutoId()).isEqualTo(produtoId);
        assertThat(movimentacao.getTipo()).isEqualTo(TipoMovimentacaoEstoque.ENTRADA);
        assertThat(movimentacao.getQuantidade()).isEqualByComparingTo("5");
        assertThat(movimentacao.getDataHora()).isNotNull();
    }

    @Test
    void rejeitaQuantidadeZeroOuNegativa() {
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoEstoque.registrar(
                produtoId, TipoMovimentacaoEstoque.ENTRADA, BigDecimal.ZERO, "motivo", null,
                OrigemMovimentacaoEstoque.MANUAL, null, null));
    }

    @Test
    void rejeitaMotivoEmBranco() {
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoEstoque.registrar(
                produtoId, TipoMovimentacaoEstoque.ENTRADA, BigDecimal.ONE, " ", null,
                OrigemMovimentacaoEstoque.MANUAL, null, null));
    }

    @Test
    void rejeitaProdutoNulo() {
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoEstoque.registrar(
                null, TipoMovimentacaoEstoque.ENTRADA, BigDecimal.ONE, "motivo", null,
                OrigemMovimentacaoEstoque.MANUAL, null, null));
    }
}
