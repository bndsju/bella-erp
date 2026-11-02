package com.bella.backend.domain.relatorio.port.in;

import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.relatorio.model.RelatoriosEstoque.RelatorioMovimentacoes;
import com.bella.backend.domain.relatorio.model.RelatoriosEstoque.RelatorioPosicao;

import java.util.UUID;

public interface ConsultarRelatoriosEstoqueUseCase {

    RelatorioMovimentacoes movimentacoes(PeriodoRelatorio periodo, UUID produtoId, TipoMovimentacaoEstoque tipo,
                                         int pagina, int tamanho);

    /** Posição atual (fotografia de hoje); {@code apenasAbaixoDoMinimo} restringe aos produtos em falta. */
    RelatorioPosicao posicao(UUID produtoId, UUID categoriaId, boolean apenasAbaixoDoMinimo, int pagina,
                             int tamanho);
}
