package com.bella.backend.application.relatorio.usecase;

import com.bella.backend.domain.categoriadespesa.port.in.BuscarCategoriaDespesaPorIdUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.in.BuscarCategoriaFluxoCaixaPorIdUseCase;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.contapagar.model.ContaPagar;
import com.bella.backend.domain.contapagar.model.ParcelaContaPagar;
import com.bella.backend.domain.contapagar.model.SituacaoContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;
import com.bella.backend.domain.contapagar.port.in.ListarContasPagarUseCase;
import com.bella.backend.domain.contareceber.model.ContaReceber;
import com.bella.backend.domain.contareceber.model.ParcelaContaReceber;
import com.bella.backend.domain.contareceber.model.SituacaoContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;
import com.bella.backend.domain.contareceber.port.in.ListarContasReceberUseCase;
import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.ResumoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.ConsultarFluxoCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.ListarMovimentacoesCaixaUseCase;
import com.bella.backend.domain.fornecedor.port.in.BuscarFornecedorPorIdUseCase;
import com.bella.backend.domain.relatorio.model.Pagina;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.FaixaVencimento;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.FluxoPorCategoria;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.LinhaConta;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.LinhaMovimentacaoCaixa;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.LinhaTitulo;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.PontoFluxo;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.RelatorioContas;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.RelatorioFluxoCaixa;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.RelatorioVencimentos;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.ResumoFluxo;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.TipoTitulo;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.TotaisContas;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.TotaisTitulos;
import com.bella.backend.domain.relatorio.port.in.ConsultarRelatoriosFinanceirosUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static com.bella.backend.application.relatorio.usecase.ApuracaoComercial.soma;

@Service
@Transactional(readOnly = true)
public class RelatoriosFinanceirosService implements ConsultarRelatoriosFinanceirosUseCase {

    private final ListarContasReceberUseCase listarContasReceberUseCase;
    private final ListarContasPagarUseCase listarContasPagarUseCase;
    private final ListarMovimentacoesCaixaUseCase listarMovimentacoesCaixaUseCase;
    private final ConsultarFluxoCaixaUseCase consultarFluxoCaixaUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase;
    private final BuscarCategoriaDespesaPorIdUseCase buscarCategoriaDespesaPorIdUseCase;
    private final BuscarCategoriaFluxoCaixaPorIdUseCase buscarCategoriaFluxoCaixaPorIdUseCase;

    public RelatoriosFinanceirosService(ListarContasReceberUseCase listarContasReceberUseCase,
                                        ListarContasPagarUseCase listarContasPagarUseCase,
                                        ListarMovimentacoesCaixaUseCase listarMovimentacoesCaixaUseCase,
                                        ConsultarFluxoCaixaUseCase consultarFluxoCaixaUseCase,
                                        BuscarClientePorIdUseCase buscarClientePorIdUseCase,
                                        BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase,
                                        BuscarCategoriaDespesaPorIdUseCase buscarCategoriaDespesaPorIdUseCase,
                                        BuscarCategoriaFluxoCaixaPorIdUseCase buscarCategoriaFluxoCaixaPorIdUseCase) {
        this.listarContasReceberUseCase = listarContasReceberUseCase;
        this.listarContasPagarUseCase = listarContasPagarUseCase;
        this.listarMovimentacoesCaixaUseCase = listarMovimentacoesCaixaUseCase;
        this.consultarFluxoCaixaUseCase = consultarFluxoCaixaUseCase;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.buscarFornecedorPorIdUseCase = buscarFornecedorPorIdUseCase;
        this.buscarCategoriaDespesaPorIdUseCase = buscarCategoriaDespesaPorIdUseCase;
        this.buscarCategoriaFluxoCaixaPorIdUseCase = buscarCategoriaFluxoCaixaPorIdUseCase;
    }

    /** Parcela e conta vistas de forma neutra, para tratar contas a receber e a pagar pelo mesmo código. */
    private record ParcelaLida(int numero, LocalDate vencimento, BigDecimal saldo) {

        boolean emAberto() {
            return saldo.signum() > 0;
        }

        boolean vencida(LocalDate hoje) {
            return emAberto() && vencimento.isBefore(hoje);
        }
    }

    private record ContaLida(TipoTitulo tipo, UUID id, String descricao, UUID parceiroId, String parceiroNome,
                             String categoriaNome, BigDecimal valorTotal, BigDecimal valorLiquidado,
                             BigDecimal saldoPendente, String status, boolean cancelada,
                             List<ParcelaLida> parcelas) {
    }

    @Override
    public RelatorioContas contasReceber(PeriodoRelatorio vencimento, UUID clienteId, StatusContaReceber status,
                                         SituacaoContaReceber situacao, int pagina, int tamanho) {
        Pagina.validar(pagina, tamanho);
        Map<UUID, String> nomes = new HashMap<>();
        List<ContaLida> contas = listarContasReceberUseCase
                .listar(new ListarContasReceberUseCase.FiltroListagem(clienteId, status, situacao,
                        vencimento.inicio(), vencimento.fim()))
                .stream()
                .map(conta -> lerContaReceber(conta, nomes))
                .toList();
        return montarRelatorioContas(vencimento, contas, status == StatusContaReceber.CANCELADA, pagina, tamanho);
    }

    @Override
    public RelatorioContas contasPagar(PeriodoRelatorio vencimento, UUID fornecedorId, UUID categoriaDespesaId,
                                       StatusContaPagar status, SituacaoContaPagar situacao, int pagina,
                                       int tamanho) {
        Pagina.validar(pagina, tamanho);
        Map<UUID, String> nomes = new HashMap<>();
        List<ContaLida> contas = listarContasPagarUseCase
                .listar(new ListarContasPagarUseCase.FiltroListagem(fornecedorId, categoriaDespesaId, status,
                        situacao, vencimento.inicio(), vencimento.fim()))
                .stream()
                .map(conta -> lerContaPagar(conta, nomes))
                .toList();
        return montarRelatorioContas(vencimento, contas, status == StatusContaPagar.CANCELADA, pagina, tamanho);
    }

    @Override
    public RelatorioVencimentos vencimentos(PeriodoRelatorio vencimento, TipoTitulo tipo, boolean apenasVencidos,
                                            int pagina, int tamanho) {
        Pagina.validar(pagina, tamanho);
        LocalDate hoje = LocalDate.now();
        Map<UUID, String> nomes = new HashMap<>();

        List<ContaLida> contas = new ArrayList<>();
        if (tipo == null || tipo == TipoTitulo.RECEBER) {
            listarContasReceberUseCase.listar(new ListarContasReceberUseCase.FiltroListagem(
                    null, null, SituacaoContaReceber.EM_ABERTO, null, null))
                    .forEach(conta -> contas.add(lerContaReceber(conta, nomes)));
        }
        if (tipo == null || tipo == TipoTitulo.PAGAR) {
            listarContasPagarUseCase.listar(new ListarContasPagarUseCase.FiltroListagem(
                    null, null, null, SituacaoContaPagar.EM_ABERTO, null, null))
                    .forEach(conta -> contas.add(lerContaPagar(conta, nomes)));
        }

        List<LinhaTitulo> titulos = new ArrayList<>();
        for (ContaLida conta : contas) {
            for (ParcelaLida parcela : conta.parcelas()) {
                if (!parcela.emAberto() || !vencimento.contem(parcela.vencimento())) {
                    continue;
                }
                if (apenasVencidos && !parcela.vencida(hoje)) {
                    continue;
                }
                titulos.add(new LinhaTitulo(conta.tipo(), conta.id(), conta.descricao(), conta.parceiroNome(),
                        parcela.numero(), parcela.vencimento(), parcela.saldo(),
                        ChronoUnit.DAYS.between(parcela.vencimento(), hoje)));
            }
        }
        titulos.sort(Comparator.comparing(LinhaTitulo::vencimento).thenComparing(LinhaTitulo::descricao));

        TotaisTitulos totais = new TotaisTitulos(
                somaTitulos(titulos, TipoTitulo.RECEBER, true),
                somaTitulos(titulos, TipoTitulo.RECEBER, false),
                somaTitulos(titulos, TipoTitulo.PAGAR, true),
                somaTitulos(titulos, TipoTitulo.PAGAR, false));

        return new RelatorioVencimentos(hoje, vencimento, totais, faixas(titulos), Pagina.de(titulos, pagina, tamanho));
    }

    @Override
    public RelatorioFluxoCaixa fluxoCaixa(PeriodoRelatorio periodo, TipoMovimentacaoCaixa tipo, UUID categoriaId,
                                          OrigemMovimentacaoCaixa origem, SituacaoMovimentacaoCaixa situacao,
                                          int pagina, int tamanho) {
        Pagina.validar(pagina, tamanho);
        PeriodoRelatorio efetivo = PeriodoRelatorio.deAtividade(periodo.inicio(), periodo.fim());

        List<MovimentacaoCaixa> movimentacoes = listarMovimentacoesCaixaUseCase.listar(
                new ListarMovimentacoesCaixaUseCase.FiltroListagem(tipo, categoriaId, origem, situacao,
                        efetivo.inicio(), efetivo.fim(), false));

        Map<LocalDate, List<MovimentacaoCaixa>> porChave = new HashMap<>();
        Map<UUID, List<MovimentacaoCaixa>> porCategoria = new LinkedHashMap<>();
        for (MovimentacaoCaixa movimentacao : movimentacoes) {
            porChave.computeIfAbsent(efetivo.chave(movimentacao.getDataReferencia()), chave -> new ArrayList<>())
                    .add(movimentacao);
            porCategoria.computeIfAbsent(movimentacao.getCategoriaId(), chave -> new ArrayList<>())
                    .add(movimentacao);
        }

        List<PontoFluxo> serie = efetivo.chaves().stream()
                .map(chave -> {
                    ResumoCaixa resumo = ResumoCaixa.de(porChave.getOrDefault(chave, List.of()));
                    return new PontoFluxo(chave, resumo.entradasRealizadas(), resumo.saidasRealizadas());
                })
                .toList();

        Map<UUID, String> nomesCategorias = new HashMap<>();
        List<FluxoPorCategoria> categorias = porCategoria.entrySet().stream()
                .map(entrada -> new FluxoPorCategoria(entrada.getKey(), nomeCategoriaCaixa(entrada.getKey(),
                        nomesCategorias), paraResumoFluxo(ResumoCaixa.de(entrada.getValue()))))
                .sorted(Comparator.comparing(FluxoPorCategoria::categoriaNome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<LinhaMovimentacaoCaixa> linhas = movimentacoes.stream()
                .sorted(Comparator.comparing(MovimentacaoCaixa::getDataReferencia).reversed()
                        .thenComparing(MovimentacaoCaixa::getCriadoEm, Comparator.reverseOrder()))
                .map(movimentacao -> new LinhaMovimentacaoCaixa(
                        movimentacao.getId(), movimentacao.getDataReferencia(), movimentacao.getTipo(),
                        movimentacao.getDescricao(),
                        movimentacao.getCategoriaId() == null
                                ? null : nomeCategoriaCaixa(movimentacao.getCategoriaId(), nomesCategorias),
                        movimentacao.getOrigem(), movimentacao.getSituacao(), movimentacao.getValor()))
                .toList();

        return new RelatorioFluxoCaixa(efetivo, efetivo.granularidade(),
                paraResumoFluxo(ResumoCaixa.de(movimentacoes)),
                consultarFluxoCaixaUseCase.consultarSaldoAcumulado(efetivo.fim()).saldoRealizado(),
                serie, categorias, Pagina.de(linhas, pagina, tamanho));
    }

    private ContaLida lerContaReceber(ContaReceber conta, Map<UUID, String> nomes) {
        String cliente = conta.getClienteId() == null ? null : nomes.computeIfAbsent(conta.getClienteId(),
                id -> buscarClientePorIdUseCase.buscarPorId(id).getNomeExibicao());
        List<ParcelaLida> parcelas = conta.getParcelas().stream()
                .map((ParcelaContaReceber p) -> new ParcelaLida(p.getNumero(), p.getVencimento(),
                        p.getSaldoPendente()))
                .toList();
        return new ContaLida(TipoTitulo.RECEBER, conta.getId(), conta.getDescricao(), conta.getClienteId(), cliente,
                null, conta.getValorTotal(), conta.getValorRecebido(), conta.getSaldoPendente(),
                conta.getStatus().name(), conta.getStatus() == StatusContaReceber.CANCELADA, parcelas);
    }

    private ContaLida lerContaPagar(ContaPagar conta, Map<UUID, String> nomes) {
        String fornecedor = conta.getFornecedorId() == null ? null : nomes.computeIfAbsent(conta.getFornecedorId(),
                id -> buscarFornecedorPorIdUseCase.buscarPorId(id).getNome());
        String categoria = nomes.computeIfAbsent(conta.getCategoriaDespesaId(),
                id -> buscarCategoriaDespesaPorIdUseCase.buscarPorId(id).getNome());
        List<ParcelaLida> parcelas = conta.getParcelas().stream()
                .map((ParcelaContaPagar p) -> new ParcelaLida(p.getNumero(), p.getVencimento(),
                        p.getSaldoPendente()))
                .toList();
        return new ContaLida(TipoTitulo.PAGAR, conta.getId(), conta.getDescricao(), conta.getFornecedorId(),
                fornecedor, categoria, conta.getValorTotal(), conta.getValorPago(), conta.getSaldoPendente(),
                conta.getStatus().name(), conta.getStatus() == StatusContaPagar.CANCELADA, parcelas);
    }

    private RelatorioContas montarRelatorioContas(PeriodoRelatorio vencimento, List<ContaLida> lidas,
                                                  boolean incluirCanceladas, int pagina, int tamanho) {
        LocalDate hoje = LocalDate.now();
        List<ContaLida> contas = lidas.stream()
                .filter(conta -> incluirCanceladas || !conta.cancelada())
                .sorted(Comparator.comparing((ContaLida conta) -> proximoVencimento(conta),
                        Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(ContaLida::descricao))
                .toList();

        BigDecimal vencido = contas.stream()
                .flatMap(conta -> conta.parcelas().stream())
                .filter(parcela -> parcela.vencida(hoje))
                .map(ParcelaLida::saldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = soma(contas, ContaLida::saldoPendente);
        TotaisContas totais = new TotaisContas(contas.size(), soma(contas, ContaLida::valorTotal),
                soma(contas, ContaLida::valorLiquidado), saldo, vencido, saldo.subtract(vencido));

        List<LinhaConta> linhas = contas.stream()
                .map(conta -> new LinhaConta(conta.id(), conta.descricao(), conta.parceiroId(),
                        conta.parceiroNome(), conta.categoriaNome(), conta.valorTotal(), conta.valorLiquidado(),
                        conta.saldoPendente(), proximoVencimento(conta), conta.status(),
                        conta.parcelas().stream().anyMatch(parcela -> parcela.vencida(hoje))))
                .toList();
        return new RelatorioContas(vencimento, totais, Pagina.de(linhas, pagina, tamanho));
    }

    private static LocalDate proximoVencimento(ContaLida conta) {
        return conta.parcelas().stream()
                .filter(ParcelaLida::emAberto)
                .map(ParcelaLida::vencimento)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private static BigDecimal somaTitulos(List<LinhaTitulo> titulos, TipoTitulo tipo, boolean vencidos) {
        return titulos.stream()
                .filter(titulo -> titulo.tipo() == tipo && (titulo.diasEmAtraso() > 0) == vencidos)
                .map(LinhaTitulo::saldoPendente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Faixas por dias de atraso, do mais atrasado ao mais distante a vencer; vencer hoje é "a vencer". */
    private static List<FaixaVencimento> faixas(List<LinhaTitulo> titulos) {
        Map<String, Function<Long, Boolean>> criterios = new LinkedHashMap<>();
        criterios.put("Vencido há mais de 90 dias", dias -> dias > 90);
        criterios.put("Vencido há 61 a 90 dias", dias -> dias >= 61 && dias <= 90);
        criterios.put("Vencido há 31 a 60 dias", dias -> dias >= 31 && dias <= 60);
        criterios.put("Vencido há 1 a 30 dias", dias -> dias >= 1 && dias <= 30);
        criterios.put("A vencer em até 7 dias", dias -> dias <= 0 && dias >= -7);
        criterios.put("A vencer em 8 a 30 dias", dias -> dias <= -8 && dias >= -30);
        criterios.put("A vencer em mais de 30 dias", dias -> dias < -30);

        List<FaixaVencimento> faixas = new ArrayList<>();
        criterios.forEach((nome, criterio) -> faixas.add(new FaixaVencimento(nome,
                somaFaixa(titulos, TipoTitulo.RECEBER, criterio), somaFaixa(titulos, TipoTitulo.PAGAR, criterio))));
        return faixas;
    }

    private static BigDecimal somaFaixa(List<LinhaTitulo> titulos, TipoTitulo tipo,
                                        Function<Long, Boolean> criterio) {
        return titulos.stream()
                .filter(titulo -> titulo.tipo() == tipo && criterio.apply(titulo.diasEmAtraso()))
                .map(LinhaTitulo::saldoPendente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String nomeCategoriaCaixa(UUID categoriaId, Map<UUID, String> cache) {
        if (categoriaId == null) {
            return "Sem categoria";
        }
        return Objects.requireNonNull(cache.computeIfAbsent(categoriaId,
                id -> buscarCategoriaFluxoCaixaPorIdUseCase.buscarPorId(id).getNome()));
    }

    private static ResumoFluxo paraResumoFluxo(ResumoCaixa resumo) {
        return new ResumoFluxo(resumo.entradasPrevistas(), resumo.saidasPrevistas(), resumo.entradasRealizadas(),
                resumo.saidasRealizadas(), resumo.saldoRealizado(), resumo.saldoProjetado());
    }
}
