package com.bella.backend.application.dashboard.usecase;

import com.bella.backend.application.relatorio.usecase.ApuracaoComercial;
import com.bella.backend.application.relatorio.usecase.ApuracaoComercial.Conversao;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.contapagar.model.ContaPagar;
import com.bella.backend.domain.contapagar.model.ParcelaContaPagar;
import com.bella.backend.domain.contapagar.model.SituacaoContaPagar;
import com.bella.backend.domain.contapagar.port.in.ListarContasPagarUseCase;
import com.bella.backend.domain.contareceber.model.ContaReceber;
import com.bella.backend.domain.contareceber.model.ParcelaContaReceber;
import com.bella.backend.domain.contareceber.model.SituacaoContaReceber;
import com.bella.backend.domain.contareceber.port.in.ListarContasReceberUseCase;
import com.bella.backend.domain.dashboard.model.Dashboard;
import com.bella.backend.domain.dashboard.model.Dashboard.ClienteDestaque;
import com.bella.backend.domain.dashboard.model.Dashboard.Comercial;
import com.bella.backend.domain.dashboard.model.Dashboard.Financeiro;
import com.bella.backend.domain.dashboard.model.Dashboard.Periodo;
import com.bella.backend.domain.dashboard.model.Dashboard.PontoFluxo;
import com.bella.backend.domain.dashboard.model.Dashboard.PontoVendas;
import com.bella.backend.domain.dashboard.model.Dashboard.PosicaoContas;
import com.bella.backend.domain.dashboard.model.Dashboard.ProdutoAbaixoDoMinimo;
import com.bella.backend.domain.dashboard.model.Dashboard.ProdutoMaisVendido;
import com.bella.backend.domain.dashboard.port.in.ConsultarDashboardUseCase;
import com.bella.backend.domain.estoque.model.PosicaoEstoqueProduto;
import com.bella.backend.domain.estoque.port.in.ListarProdutosAbaixoDoEstoqueMinimoUseCase;
import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.ResumoCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.ConsultarFluxoCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.ListarMovimentacoesCaixaUseCase;
import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.venda.model.Venda;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.bella.backend.application.relatorio.usecase.ApuracaoComercial.soma;

/**
 * Monta o painel inicial. Vendas, rankings e conversão vêm de {@link ApuracaoComercial}, a mesma
 * apuração dos relatórios gerenciais, para que os números do painel e dos relatórios coincidam.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService implements ConsultarDashboardUseCase {

    private final ApuracaoComercial apuracao;
    private final ListarContasReceberUseCase listarContasReceberUseCase;
    private final ListarContasPagarUseCase listarContasPagarUseCase;
    private final ConsultarFluxoCaixaUseCase consultarFluxoCaixaUseCase;
    private final ListarMovimentacoesCaixaUseCase listarMovimentacoesCaixaUseCase;
    private final ListarProdutosAbaixoDoEstoqueMinimoUseCase listarProdutosAbaixoDoEstoqueMinimoUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    public DashboardService(ApuracaoComercial apuracao,
                            ListarContasReceberUseCase listarContasReceberUseCase,
                            ListarContasPagarUseCase listarContasPagarUseCase,
                            ConsultarFluxoCaixaUseCase consultarFluxoCaixaUseCase,
                            ListarMovimentacoesCaixaUseCase listarMovimentacoesCaixaUseCase,
                            ListarProdutosAbaixoDoEstoqueMinimoUseCase listarProdutosAbaixoDoEstoqueMinimoUseCase,
                            BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase,
                            BuscarClientePorIdUseCase buscarClientePorIdUseCase) {
        this.apuracao = apuracao;
        this.listarContasReceberUseCase = listarContasReceberUseCase;
        this.listarContasPagarUseCase = listarContasPagarUseCase;
        this.consultarFluxoCaixaUseCase = consultarFluxoCaixaUseCase;
        this.listarMovimentacoesCaixaUseCase = listarMovimentacoesCaixaUseCase;
        this.listarProdutosAbaixoDoEstoqueMinimoUseCase = listarProdutosAbaixoDoEstoqueMinimoUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
    }

    @Override
    public Dashboard consultar(LocalDate inicio, LocalDate fim, int limiteRankings) {
        LocalDate hoje = LocalDate.now();
        PeriodoRelatorio periodoRelatorio = PeriodoRelatorio.deAtividade(inicio, fim);
        int limite = Math.max(1, limiteRankings);

        Periodo periodo = new Periodo(periodoRelatorio.inicio(), periodoRelatorio.fim(),
                periodoRelatorio.granularidade());
        List<Venda> vendasDoPeriodo = apuracao.vendasConcluidas(periodoRelatorio, null);
        List<ProdutoAbaixoDoMinimo> abaixoDoMinimo = produtosAbaixoDoMinimo();

        return new Dashboard(
                periodo,
                comercial(periodoRelatorio, vendasDoPeriodo),
                financeiro(periodoRelatorio, hoje),
                abaixoDoMinimo.stream().limit(limite).toList(),
                abaixoDoMinimo.size(),
                apuracao.produtosMaisVendidos(vendasDoPeriodo, id -> true, limite).stream()
                        .map(total -> new ProdutoMaisVendido(total.produtoId(),
                                buscarProdutoPorIdUseCase.buscarPorId(total.produtoId()).getNome(),
                                total.quantidade(), total.valor()))
                        .toList(),
                apuracao.principaisClientes(vendasDoPeriodo, limite).stream()
                        .map(total -> new ClienteDestaque(total.clienteId(),
                                buscarClientePorIdUseCase.buscarPorId(total.clienteId()).getNomeExibicao(),
                                total.quantidadeVendas(), total.valor()))
                        .toList());
    }

    private Comercial comercial(PeriodoRelatorio periodo, List<Venda> vendas) {
        BigDecimal faturamento = soma(vendas, Venda::getValorTotal);
        BigDecimal ticketMedio = vendas.isEmpty()
                ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(vendas.size()), 2, RoundingMode.HALF_UP);

        List<Pedido> pedidos = apuracao.pedidos(periodo, null);
        int cancelados = (int) pedidos.stream().filter(pedido -> pedido.getStatus() == StatusPedido.CANCELADO).count();
        Conversao conversao = apuracao.conversao(periodo, null);

        return new Comercial(
                faturamento,
                vendas.size(),
                ticketMedio,
                pedidos.size() - cancelados,
                cancelados,
                conversao.emitidos().size(),
                conversao.valorEmitidos(),
                conversao.taxaConversaoPedidos(),
                conversao.taxaConversaoVendas(),
                vendasPorPeriodo(periodo, vendas));
    }

    private List<PontoVendas> vendasPorPeriodo(PeriodoRelatorio periodo, List<Venda> vendas) {
        Map<LocalDate, BigDecimal> valores = new LinkedHashMap<>();
        Map<LocalDate, Integer> quantidades = new HashMap<>();
        for (LocalDate chave : periodo.chaves()) {
            valores.put(chave, BigDecimal.ZERO);
        }
        for (Venda venda : vendas) {
            LocalDate chave = periodo.chave(venda.getCriadoEm().atZone(ZoneId.systemDefault()).toLocalDate());
            valores.merge(chave, venda.getValorTotal(), BigDecimal::add);
            quantidades.merge(chave, 1, Integer::sum);
        }
        return valores.entrySet().stream()
                .map(entrada -> new PontoVendas(entrada.getKey(), entrada.getValue(),
                        quantidades.getOrDefault(entrada.getKey(), 0)))
                .toList();
    }

    private Financeiro financeiro(PeriodoRelatorio periodo, LocalDate hoje) {
        ResumoCaixa resumo = consultarFluxoCaixaUseCase.consultarResumo(periodo.inicio(), periodo.fim());
        ResumoCaixa acumulado = consultarFluxoCaixaUseCase.consultarSaldoAcumulado(hoje);

        List<MovimentacaoCaixa> movimentacoes = listarMovimentacoesCaixaUseCase.listar(
                new ListarMovimentacoesCaixaUseCase.FiltroListagem(null, null, null, null,
                        periodo.inicio(), periodo.fim(), false));
        Map<LocalDate, List<MovimentacaoCaixa>> porChave = new HashMap<>();
        for (MovimentacaoCaixa movimentacao : movimentacoes) {
            porChave.computeIfAbsent(periodo.chave(movimentacao.getDataReferencia()), chave -> new ArrayList<>())
                    .add(movimentacao);
        }
        List<PontoFluxo> fluxo = periodo.chaves().stream()
                .map(chave -> {
                    ResumoCaixa resumoDoPonto = ResumoCaixa.de(porChave.getOrDefault(chave, List.of()));
                    return new PontoFluxo(chave, resumoDoPonto.entradasRealizadas(), resumoDoPonto.saidasRealizadas());
                })
                .toList();

        return new Financeiro(
                posicaoReceber(periodo, hoje),
                posicaoPagar(periodo, hoje),
                acumulado.saldoRealizado(),
                resumo.entradasRealizadas(),
                resumo.saidasRealizadas(),
                resumo.saldoRealizado(),
                resumo.entradasPrevistas(),
                resumo.saidasPrevistas(),
                fluxo);
    }

    private PosicaoContas posicaoReceber(PeriodoRelatorio periodo, LocalDate hoje) {
        List<ContaReceber> contas = listarContasReceberUseCase.listar(new ListarContasReceberUseCase.FiltroListagem(
                null, null, SituacaoContaReceber.EM_ABERTO, null, null));
        List<Parcela> parcelas = new ArrayList<>();
        for (ContaReceber conta : contas) {
            for (ParcelaContaReceber parcela : conta.getParcelas()) {
                parcelas.add(new Parcela(parcela.getVencimento(), parcela.getSaldoPendente(),
                        parcela.estaVencida(hoje)));
            }
        }
        int vencidas = (int) contas.stream().filter(conta -> conta.estaVencida(hoje)).count();
        return posicao(periodo, parcelas, contas.size(), vencidas);
    }

    private PosicaoContas posicaoPagar(PeriodoRelatorio periodo, LocalDate hoje) {
        List<ContaPagar> contas = listarContasPagarUseCase.listar(new ListarContasPagarUseCase.FiltroListagem(
                null, null, null, SituacaoContaPagar.EM_ABERTO, null, null));
        List<Parcela> parcelas = new ArrayList<>();
        for (ContaPagar conta : contas) {
            for (ParcelaContaPagar parcela : conta.getParcelas()) {
                parcelas.add(new Parcela(parcela.getVencimento(), parcela.getSaldoPendente(),
                        parcela.estaVencida(hoje)));
            }
        }
        int vencidas = (int) contas.stream().filter(conta -> conta.estaVencida(hoje)).count();
        return posicao(periodo, parcelas, contas.size(), vencidas);
    }

    private PosicaoContas posicao(PeriodoRelatorio periodo, List<Parcela> parcelas, int quantidadeContas,
                                  int quantidadeVencidas) {
        BigDecimal emAberto = soma(parcelas, Parcela::saldo);
        BigDecimal vencido = soma(parcelas.stream().filter(Parcela::vencida).toList(), Parcela::saldo);
        BigDecimal noPeriodo = soma(parcelas.stream()
                .filter(parcela -> periodo.contem(parcela.vencimento()))
                .toList(), Parcela::saldo);
        return new PosicaoContas(emAberto, vencido, emAberto.subtract(vencido), noPeriodo, quantidadeContas,
                quantidadeVencidas);
    }

    private List<ProdutoAbaixoDoMinimo> produtosAbaixoDoMinimo() {
        return listarProdutosAbaixoDoEstoqueMinimoUseCase.listarAbaixoDoMinimo().stream()
                .sorted(Comparator.comparing(
                        (PosicaoEstoqueProduto posicao) -> posicao.quantidadeDisponivel()
                                .subtract(posicao.estoqueMinimo())))
                .map(posicao -> new ProdutoAbaixoDoMinimo(
                        posicao.produtoId(),
                        buscarProdutoPorIdUseCase.buscarPorId(posicao.produtoId()).getNome(),
                        posicao.quantidadeDisponivel(),
                        posicao.estoqueMinimo()))
                .toList();
    }

    private record Parcela(LocalDate vencimento, BigDecimal saldo, boolean vencida) {
    }
}
