package com.bella.backend.domain.relatorio.model;

import com.bella.backend.domain.estoque.model.OrigemMovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Relatórios de estoque: consultas sobre saldos e movimentações do módulo de estoque. */
public final class RelatoriosEstoque {

    private RelatoriosEstoque() {
    }

    public record LinhaMovimentacao(UUID id, Instant dataHora, UUID produtoId, String produtoNome,
                                    TipoMovimentacaoEstoque tipo, BigDecimal quantidade,
                                    OrigemMovimentacaoEstoque origem, String motivo, String usuarioResponsavel) {
    }

    public record TotalPorTipo(TipoMovimentacaoEstoque tipo, int quantidadeMovimentacoes,
                               BigDecimal quantidadeTotal) {
    }

    public record RelatorioMovimentacoes(PeriodoRelatorio periodo, List<TotalPorTipo> totaisPorTipo,
                                         Pagina<LinhaMovimentacao> movimentacoes) {
    }

    public record LinhaPosicao(UUID produtoId, String codigoInterno, String nome, UUID categoriaId,
                               String categoriaNome, BigDecimal estoqueFisico, BigDecimal quantidadeReservada,
                               BigDecimal quantidadeDisponivel, BigDecimal estoqueMinimo,
                               boolean abaixoDoMinimo) {
    }

    public record RelatorioPosicao(int totalProdutos, int produtosAbaixoDoMinimo, Pagina<LinhaPosicao> produtos) {
    }
}
