package com.bella.backend.application.dashboard.usecase;

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
import com.bella.backend.domain.dashboard.model.Dashboard.Granularidade;
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
import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.StatusOrcamento;
import com.bella.backend.domain.orcamento.port.in.ListarOrcamentosUseCase;
import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.pedido.port.in.ListarPedidosUseCase;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.venda.model.StatusVenda;
import com.bella.backend.domain.venda.model.Venda;
import com.bella.backend.domain.venda.model.VendaItem;
import com.bella.backend.domain.venda.port.in.ListarVendasUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
public class DashboardService implements ConsultarDashboardUseCase {

    private static final long LIMITE_DIAS_SERIE_DIARIA = 62;
    private static final Set<StatusOrcamento> STATUS_EMITIDOS = Set.of(
            StatusOrcamento.ENVIADO, StatusOrcamento.APROVADO, StatusOrcamento.RECUSADO, StatusOrcamento.EXPIRADO);

    private final ListarVendasUseCase listarVendasUseCase;
    private final ListarPedidosUseCase listarPedidosUseCase;
    private final ListarOrcamentosUseCase listarOrcamentosUseCase;
    private final ListarContasReceberUseCase listarContasReceberUseCase;
    private final ListarContasPagarUseCase listarContasPagarUseCase;
    private final ConsultarFluxoCaixaUseCase consultarFluxoCaixaUseCase;
    private final ListarMovimentacoesCaixaUseCase listarMovimentacoesCaixaUseCase;
    private final ListarProdutosAbaixoDoEstoqueMinimoUseCase listarProdutosAbaixoDoEstoqueMinimoUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    public DashboardService(ListarVendasUseCase listarVendasUseCase,
                            ListarPedidosUseCase listarPedidosUseCase,
                            ListarOrcamentosUseCase listarOrcamentosUseCase,
                            ListarContasReceberUseCase listarContasReceberUseCase,
                            ListarContasPagarUseCase listarContasPagarUseCase,
                            ConsultarFluxoCaixaUseCase consultarFluxoCaixaUseCase,
                            ListarMovimentacoesCaixaUseCase listarMovimentacoesCaixaUseCase,
                            ListarProdutosAbaixoDoEstoqueMinimoUseCase listarProdutosAbaixoDoEstoqueMinimoUseCase,
                            BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase,
                            BuscarClientePorIdUseCase buscarClientePorIdUseCase) {
        this.listarVendasUseCase = listarVendasUseCase;
        this.listarPedidosUseCase = listarPedidosUseCase;
        this.listarOrcamentosUseCase = listarOrcamentosUseCase;
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
        LocalDate de = inicio != null ? inicio : hoje.withDayOfMonth(1);
        LocalDate ate = fim != null ? fim : de.withDayOfMonth(de.lengthOfMonth());
        if (ate.isBefore(de)) {
            throw new RegraDeNegocioException("A data final do período não pode ser anterior à data inicial");
        }
        int limite = Math.max(1, limiteRankings);

        Granularidade granularidade = de.plusDays(LIMITE_DIAS_SERIE_DIARIA).isBefore(ate)
                ? Granularidade.MES
                : Granularidade.DIA;
        Periodo periodo = new Periodo(de, ate, granularidade);

        List<Venda> vendasDoPeriodo = listarVendasUseCase
                .listar(new ListarVendasUseCase.FiltroListagem(null, StatusVenda.CONCLUIDA)).stream()
                .filter(venda -> noPeriodo(venda.getCriadoEm(), de, ate))
                .toList();

        List<ProdutoAbaixoDoMinimo> abaixoDoMinimo = produtosAbaixoDoMinimo();

        return new Dashboard(
                periodo,
                comercial(periodo, vendasDoPeriodo),
                financeiro(periodo, hoje),
                abaixoDoMinimo.stream().limit(limite).toList(),
                abaixoDoMinimo.size(),
                produtosMaisVendidos(vendasDoPeriodo, limite),
                principaisClientes(vendasDoPeriodo, limite));
    }

    private Comercial comercial(Periodo periodo, List<Venda> vendas) {
        BigDecimal faturamento = soma(vendas, Venda::getValorTotal);
        BigDecimal ticketMedio = vendas.isEmpty()
                ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(vendas.size()), 2, RoundingMode.HALF_UP);

        List<Pedido> todosPedidos = listarPedidosUseCase
                .listar(new ListarPedidosUseCase.FiltroListagem(null, null));
        List<Pedido> pedidosDoPeriodo = todosPedidos.stream()
                .filter(pedido -> noPeriodo(pedido.getCriadoEm(), periodo.inicio(), periodo.fim()))
                .toList();
        int cancelados = (int) pedidosDoPeriodo.stream()
                .filter(pedido -> pedido.getStatus() == StatusPedido.CANCELADO).count();

        List<Orcamento> emitidos = listarOrcamentosUseCase
                .listar(new ListarOrcamentosUseCase.FiltroListagem(null, null)).stream()
                .filter(orcamento -> STATUS_EMITIDOS.contains(orcamento.getStatus()))
                .filter(orcamento -> noPeriodo(orcamento.getCriadoEm(), periodo.inicio(), periodo.fim()))
                .toList();

        Set<UUID> pedidosComVendaConcluida = new HashSet<>();
        for (Venda venda : listarVendasUseCase
                .listar(new ListarVendasUseCase.FiltroListagem(null, StatusVenda.CONCLUIDA))) {
            pedidosComVendaConcluida.add(venda.getPedidoId());
        }
        Map<UUID, List<Pedido>> pedidosPorOrcamento = new HashMap<>();
        for (Pedido pedido : todosPedidos) {
            if (pedido.getOrcamentoOrigemId() != null && pedido.getStatus() != StatusPedido.CANCELADO) {
                pedidosPorOrcamento.computeIfAbsent(pedido.getOrcamentoOrigemId(), chave -> new ArrayList<>())
                        .add(pedido);
            }
        }
        long convertidosEmPedido = emitidos.stream()
                .filter(orcamento -> pedidosPorOrcamento.containsKey(orcamento.getId())).count();
        long convertidosEmVenda = emitidos.stream()
                .filter(orcamento -> pedidosPorOrcamento.getOrDefault(orcamento.getId(), List.of()).stream()
                        .anyMatch(pedido -> pedidosComVendaConcluida.contains(pedido.getId())))
                .count();

        return new Comercial(
                faturamento,
                vendas.size(),
                ticketMedio,
                pedidosDoPeriodo.size() - cancelados,
                cancelados,
                emitidos.size(),
                soma(emitidos, Orcamento::getValorTotal),
                percentual(convertidosEmPedido, emitidos.size()),
                percentual(convertidosEmVenda, emitidos.size()),
                vendasPorPeriodo(periodo, vendas));
    }

    private List<PontoVendas> vendasPorPeriodo(Periodo periodo, List<Venda> vendas) {
        Map<LocalDate, BigDecimal> valores = new LinkedHashMap<>();
        Map<LocalDate, Integer> quantidades = new HashMap<>();
        for (LocalDate chave : chaves(periodo)) {
            valores.put(chave, BigDecimal.ZERO);
        }
        for (Venda venda : vendas) {
            LocalDate chave = chave(periodo, paraData(venda.getCriadoEm()));
            valores.merge(chave, venda.getValorTotal(), BigDecimal::add);
            quantidades.merge(chave, 1, Integer::sum);
        }
        return valores.entrySet().stream()
                .map(entrada -> new PontoVendas(entrada.getKey(), entrada.getValue(),
                        quantidades.getOrDefault(entrada.getKey(), 0)))
                .toList();
    }

    private Financeiro financeiro(Periodo periodo, LocalDate hoje) {
        ResumoCaixa resumo = consultarFluxoCaixaUseCase.consultarResumo(periodo.inicio(), periodo.fim());
        ResumoCaixa acumulado = consultarFluxoCaixaUseCase.consultarSaldoAcumulado(hoje);

        List<MovimentacaoCaixa> movimentacoes = listarMovimentacoesCaixaUseCase.listar(
                new ListarMovimentacoesCaixaUseCase.FiltroListagem(null, null, null, null,
                        periodo.inicio(), periodo.fim(), false));
        Map<LocalDate, List<MovimentacaoCaixa>> porChave = new HashMap<>();
        for (MovimentacaoCaixa movimentacao : movimentacoes) {
            porChave.computeIfAbsent(chave(periodo, movimentacao.getDataReferencia()), chave -> new ArrayList<>())
                    .add(movimentacao);
        }
        List<PontoFluxo> fluxo = chaves(periodo).stream()
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

    private PosicaoContas posicaoReceber(Periodo periodo, LocalDate hoje) {
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

    private PosicaoContas posicaoPagar(Periodo periodo, LocalDate hoje) {
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

    private PosicaoContas posicao(Periodo periodo, List<Parcela> parcelas, int quantidadeContas,
                                  int quantidadeVencidas) {
        BigDecimal emAberto = soma(parcelas, Parcela::saldo);
        BigDecimal vencido = soma(parcelas.stream().filter(Parcela::vencida).toList(), Parcela::saldo);
        BigDecimal noPeriodo = soma(parcelas.stream()
                .filter(parcela -> !parcela.vencimento().isBefore(periodo.inicio())
                        && !parcela.vencimento().isAfter(periodo.fim()))
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

    private List<ProdutoMaisVendido> produtosMaisVendidos(List<Venda> vendas, int limite) {
        Map<UUID, BigDecimal> quantidades = new HashMap<>();
        Map<UUID, BigDecimal> valores = new HashMap<>();
        for (Venda venda : vendas) {
            for (VendaItem item : venda.getItens()) {
                quantidades.merge(item.getProdutoId(), item.getQuantidade(), BigDecimal::add);
                valores.merge(item.getProdutoId(), item.getSubtotal(), BigDecimal::add);
            }
        }
        return quantidades.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .limit(limite)
                .map(entrada -> new ProdutoMaisVendido(
                        entrada.getKey(),
                        buscarProdutoPorIdUseCase.buscarPorId(entrada.getKey()).getNome(),
                        entrada.getValue(),
                        valores.get(entrada.getKey())))
                .toList();
    }

    private List<ClienteDestaque> principaisClientes(List<Venda> vendas, int limite) {
        Map<UUID, BigDecimal> valores = new HashMap<>();
        Map<UUID, Integer> quantidades = new HashMap<>();
        for (Venda venda : vendas) {
            valores.merge(venda.getClienteId(), venda.getValorTotal(), BigDecimal::add);
            quantidades.merge(venda.getClienteId(), 1, Integer::sum);
        }
        return valores.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .limit(limite)
                .map(entrada -> new ClienteDestaque(
                        entrada.getKey(),
                        buscarClientePorIdUseCase.buscarPorId(entrada.getKey()).getNomeExibicao(),
                        quantidades.get(entrada.getKey()),
                        entrada.getValue()))
                .toList();
    }

    private record Parcela(LocalDate vencimento, BigDecimal saldo, boolean vencida) {
    }

    private static <T> BigDecimal soma(List<T> itens, Function<T, BigDecimal> valor) {
        return itens.stream().map(valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal percentual(long parte, int total) {
        if (total == 0) {
            return null;
        }
        return BigDecimal.valueOf(parte * 100L).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private static LocalDate paraData(Instant instante) {
        return instante.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static boolean noPeriodo(Instant instante, LocalDate inicio, LocalDate fim) {
        LocalDate data = paraData(instante);
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }

    /** Início do intervalo (dia ou primeiro dia do mês) em que a data cai na série. */
    private static LocalDate chave(Periodo periodo, LocalDate data) {
        return periodo.granularidade() == Granularidade.MES ? data.withDayOfMonth(1) : data;
    }

    /** Todos os intervalos do período, inclusive os sem movimento, para a série não ter buracos. */
    private static List<LocalDate> chaves(Periodo periodo) {
        List<LocalDate> chaves = new ArrayList<>();
        LocalDate atual = chave(periodo, periodo.inicio());
        while (!atual.isAfter(periodo.fim())) {
            chaves.add(atual);
            atual = periodo.granularidade() == Granularidade.MES ? atual.plusMonths(1) : atual.plusDays(1);
        }
        return chaves;
    }
}
