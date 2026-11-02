package com.bella.backend.application.relatorio.usecase;

import com.bella.backend.domain.categoria.port.in.BuscarCategoriaPorIdUseCase;
import com.bella.backend.domain.estoque.model.MovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.PosicaoEstoqueProduto;
import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
import com.bella.backend.domain.estoque.port.in.ListarMovimentacoesEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.ListarSaldoEstoqueUseCase;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.relatorio.model.Pagina;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.relatorio.model.RelatoriosEstoque.LinhaMovimentacao;
import com.bella.backend.domain.relatorio.model.RelatoriosEstoque.LinhaPosicao;
import com.bella.backend.domain.relatorio.model.RelatoriosEstoque.RelatorioMovimentacoes;
import com.bella.backend.domain.relatorio.model.RelatoriosEstoque.RelatorioPosicao;
import com.bella.backend.domain.relatorio.model.RelatoriosEstoque.TotalPorTipo;
import com.bella.backend.domain.relatorio.port.in.ConsultarRelatoriosEstoqueUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RelatoriosEstoqueService implements ConsultarRelatoriosEstoqueUseCase {

    private final ListarMovimentacoesEstoqueUseCase listarMovimentacoesEstoqueUseCase;
    private final ListarSaldoEstoqueUseCase listarSaldoEstoqueUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase;

    public RelatoriosEstoqueService(ListarMovimentacoesEstoqueUseCase listarMovimentacoesEstoqueUseCase,
                                    ListarSaldoEstoqueUseCase listarSaldoEstoqueUseCase,
                                    BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase,
                                    BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase) {
        this.listarMovimentacoesEstoqueUseCase = listarMovimentacoesEstoqueUseCase;
        this.listarSaldoEstoqueUseCase = listarSaldoEstoqueUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
        this.buscarCategoriaPorIdUseCase = buscarCategoriaPorIdUseCase;
    }

    @Override
    public RelatorioMovimentacoes movimentacoes(PeriodoRelatorio periodo, UUID produtoId,
                                                TipoMovimentacaoEstoque tipo, int pagina, int tamanho) {
        Pagina.validar(pagina, tamanho);
        PeriodoRelatorio efetivo = PeriodoRelatorio.deAtividade(periodo.inicio(), periodo.fim());

        List<MovimentacaoEstoque> movimentacoes = listarMovimentacoesEstoqueUseCase
                .listar(new ListarMovimentacoesEstoqueUseCase.FiltroListagem(
                        produtoId, tipo, efetivo.inicioComoInstante(), efetivo.fimComoInstante().minusNanos(1)))
                .stream()
                .sorted(Comparator.comparing(MovimentacaoEstoque::getDataHora).reversed())
                .toList();

        List<TotalPorTipo> totais = Arrays.stream(TipoMovimentacaoEstoque.values())
                .map(tipoMovimentacao -> {
                    List<MovimentacaoEstoque> doTipo = movimentacoes.stream()
                            .filter(movimentacao -> movimentacao.getTipo() == tipoMovimentacao).toList();
                    return new TotalPorTipo(tipoMovimentacao, doTipo.size(),
                            ApuracaoComercial.soma(doTipo, MovimentacaoEstoque::getQuantidade));
                })
                .filter(total -> total.quantidadeMovimentacoes() > 0)
                .toList();

        Map<UUID, String> nomes = new HashMap<>();
        List<LinhaMovimentacao> linhas = movimentacoes.stream()
                .map(movimentacao -> new LinhaMovimentacao(
                        movimentacao.getId(),
                        movimentacao.getDataHora(),
                        movimentacao.getProdutoId(),
                        nomes.computeIfAbsent(movimentacao.getProdutoId(),
                                id -> buscarProdutoPorIdUseCase.buscarPorId(id).getNome()),
                        movimentacao.getTipo(),
                        movimentacao.getQuantidade(),
                        movimentacao.getOrigem(),
                        movimentacao.getMotivo(),
                        movimentacao.getUsuarioResponsavel()))
                .toList();

        return new RelatorioMovimentacoes(efetivo, totais, Pagina.de(linhas, pagina, tamanho));
    }

    @Override
    public RelatorioPosicao posicao(UUID produtoId, UUID categoriaId, boolean apenasAbaixoDoMinimo, int pagina,
                                    int tamanho) {
        Pagina.validar(pagina, tamanho);
        Map<UUID, Produto> produtos = new HashMap<>();

        List<PosicaoEstoqueProduto> filtradas = listarSaldoEstoqueUseCase.listarTodos().stream()
                .filter(posicao -> produtoId == null || posicao.produtoId().equals(produtoId))
                .filter(posicao -> categoriaId == null || categoriaId.equals(
                        produtos.computeIfAbsent(posicao.produtoId(), buscarProdutoPorIdUseCase::buscarPorId)
                                .getCategoriaId()))
                .toList();
        int abaixo = (int) filtradas.stream().filter(PosicaoEstoqueProduto::abaixoDoMinimo).count();

        List<LinhaPosicao> linhas = filtradas.stream()
                .filter(posicao -> !apenasAbaixoDoMinimo || posicao.abaixoDoMinimo())
                .map(posicao -> {
                    Produto produto = produtos.computeIfAbsent(posicao.produtoId(),
                            buscarProdutoPorIdUseCase::buscarPorId);
                    return new LinhaPosicao(
                            produto.getId(), produto.getCodigoInterno(), produto.getNome(), produto.getCategoriaId(),
                            buscarCategoriaPorIdUseCase.buscarPorId(produto.getCategoriaId()).getNome(),
                            posicao.estoqueFisico(), posicao.quantidadeReservada(), posicao.quantidadeDisponivel(),
                            posicao.estoqueMinimo(), posicao.abaixoDoMinimo());
                })
                .sorted(Comparator.comparing(LinhaPosicao::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new RelatorioPosicao(filtradas.size(), abaixo, Pagina.de(linhas, pagina, tamanho));
    }
}
