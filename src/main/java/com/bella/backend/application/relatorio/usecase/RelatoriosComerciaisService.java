package com.bella.backend.application.relatorio.usecase;

import com.bella.backend.application.relatorio.usecase.ApuracaoComercial.Conversao;
import com.bella.backend.domain.categoria.port.in.BuscarCategoriaPorIdUseCase;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.StatusOrcamento;
import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.relatorio.model.Pagina;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.LinhaClienteMaiorVolume;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.LinhaOrcamentosPorStatus;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.LinhaPedidosPorStatus;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.LinhaProdutoMaisVendido;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.LinhaVenda;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.PontoVendas;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioClientesMaiorVolume;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioOrcamentos;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioPedidosPorStatus;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioProdutosMaisVendidos;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioVendas;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.ResumoVendas;
import com.bella.backend.domain.relatorio.port.in.ConsultarRelatoriosComerciaisUseCase;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.venda.model.FormaPagamento;
import com.bella.backend.domain.venda.model.Venda;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bella.backend.application.relatorio.usecase.ApuracaoComercial.percentual;
import static com.bella.backend.application.relatorio.usecase.ApuracaoComercial.soma;

@Service
@Transactional(readOnly = true)
public class RelatoriosComerciaisService implements ConsultarRelatoriosComerciaisUseCase {

    private static final int LIMITE_MAXIMO_RANKING = 100;

    private final ApuracaoComercial apuracao;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase;

    public RelatoriosComerciaisService(ApuracaoComercial apuracao,
                                       BuscarClientePorIdUseCase buscarClientePorIdUseCase,
                                       BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase,
                                       BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase) {
        this.apuracao = apuracao;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
        this.buscarCategoriaPorIdUseCase = buscarCategoriaPorIdUseCase;
    }

    @Override
    public RelatorioVendas vendas(PeriodoRelatorio periodo, UUID clienteId, UUID produtoId,
                                  FormaPagamento formaPagamento, int pagina, int tamanho) {
        Pagina.validar(pagina, tamanho);
        PeriodoRelatorio efetivo = PeriodoRelatorio.deAtividade(periodo.inicio(), periodo.fim());

        List<Venda> vendas = apuracao.vendasConcluidas(efetivo, clienteId).stream()
                .filter(venda -> formaPagamento == null || venda.getFormaPagamento() == formaPagamento)
                .filter(venda -> produtoId == null
                        || venda.getItens().stream().anyMatch(item -> item.getProdutoId().equals(produtoId)))
                .sorted(Comparator.comparing(Venda::getCriadoEm).reversed())
                .toList();

        BigDecimal faturamento = soma(vendas, Venda::getValorTotal);
        BigDecimal ticketMedio = vendas.isEmpty()
                ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(vendas.size()), 2, RoundingMode.HALF_UP);
        ResumoVendas resumo = new ResumoVendas(vendas.size(), faturamento, ticketMedio,
                soma(vendas, Venda::getValorDesconto), soma(vendas, Venda::getValorFrete));

        Map<UUID, String> nomesClientes = new HashMap<>();
        List<LinhaVenda> linhas = vendas.stream()
                .map(venda -> new LinhaVenda(
                        venda.getId(),
                        venda.getCriadoEm().atZone(ZoneId.systemDefault()).toLocalDate(),
                        venda.getClienteId(),
                        nomesClientes.computeIfAbsent(venda.getClienteId(), this::nomeCliente),
                        venda.getFormaPagamento(),
                        venda.getValorDesconto(),
                        venda.getValorFrete(),
                        venda.getValorTotal()))
                .toList();

        return new RelatorioVendas(efetivo, efetivo.granularidade(), resumo, serieDeVendas(efetivo, vendas),
                Pagina.de(linhas, pagina, tamanho));
    }

    @Override
    public RelatorioPedidosPorStatus pedidosPorStatus(PeriodoRelatorio periodo, UUID clienteId) {
        PeriodoRelatorio efetivo = PeriodoRelatorio.deAtividade(periodo.inicio(), periodo.fim());
        List<Pedido> pedidos = apuracao.pedidos(efetivo, clienteId);

        List<LinhaPedidosPorStatus> linhas = Arrays.stream(StatusPedido.values())
                .map(status -> {
                    List<Pedido> doStatus = pedidos.stream().filter(pedido -> pedido.getStatus() == status).toList();
                    return new LinhaPedidosPorStatus(status, doStatus.size(), soma(doStatus, Pedido::getValorTotal));
                })
                .toList();
        List<LinhaPedidosPorStatus> validos = linhas.stream()
                .filter(linha -> linha.status() != StatusPedido.CANCELADO)
                .toList();

        return new RelatorioPedidosPorStatus(efetivo, linhas,
                validos.stream().mapToInt(LinhaPedidosPorStatus::quantidade).sum(),
                soma(validos, LinhaPedidosPorStatus::valorTotal));
    }

    @Override
    public RelatorioOrcamentos orcamentos(PeriodoRelatorio periodo, UUID clienteId) {
        PeriodoRelatorio efetivo = PeriodoRelatorio.deAtividade(periodo.inicio(), periodo.fim());
        List<Orcamento> orcamentos = apuracao.orcamentos(efetivo, clienteId);

        List<LinhaOrcamentosPorStatus> linhas = Arrays.stream(StatusOrcamento.values())
                .map(status -> {
                    List<Orcamento> doStatus = orcamentos.stream()
                            .filter(orcamento -> orcamento.getStatus() == status).toList();
                    return new LinhaOrcamentosPorStatus(status, doStatus.size(),
                            soma(doStatus, Orcamento::getValorTotal));
                })
                .toList();

        Conversao conversao = apuracao.conversao(efetivo, clienteId);
        int aprovados = (int) conversao.emitidos().stream()
                .filter(orcamento -> orcamento.getStatus() == StatusOrcamento.APROVADO).count();
        int recusados = (int) conversao.emitidos().stream()
                .filter(orcamento -> orcamento.getStatus() == StatusOrcamento.RECUSADO).count();

        return new RelatorioOrcamentos(efetivo, linhas, conversao.emitidos().size(), conversao.valorEmitidos(),
                aprovados, recusados, percentual(aprovados, conversao.emitidos().size()),
                conversao.taxaConversaoPedidos(), conversao.taxaConversaoVendas());
    }

    @Override
    public RelatorioProdutosMaisVendidos produtosMaisVendidos(PeriodoRelatorio periodo, UUID clienteId,
                                                              UUID categoriaId, int limite) {
        validarLimite(limite);
        PeriodoRelatorio efetivo = PeriodoRelatorio.deAtividade(periodo.inicio(), periodo.fim());
        List<Venda> vendas = apuracao.vendasConcluidas(efetivo, clienteId);

        Map<UUID, Produto> produtos = new HashMap<>();
        List<LinhaProdutoMaisVendido> linhas = apuracao
                .produtosMaisVendidos(vendas,
                        id -> categoriaId == null
                                || categoriaId.equals(produtos.computeIfAbsent(id, buscarProdutoPorIdUseCase::buscarPorId)
                                .getCategoriaId()),
                        limite)
                .stream()
                .map(total -> {
                    Produto produto = produtos.computeIfAbsent(total.produtoId(), buscarProdutoPorIdUseCase::buscarPorId);
                    return new LinhaProdutoMaisVendido(
                            produto.getId(), produto.getCodigoInterno(), produto.getNome(),
                            produto.getCategoriaId(),
                            buscarCategoriaPorIdUseCase.buscarPorId(produto.getCategoriaId()).getNome(),
                            total.quantidade(), total.valor(), total.quantidadeVendas());
                })
                .toList();
        return new RelatorioProdutosMaisVendidos(efetivo, limite, linhas);
    }

    @Override
    public RelatorioClientesMaiorVolume clientesMaiorVolume(PeriodoRelatorio periodo, int limite) {
        validarLimite(limite);
        PeriodoRelatorio efetivo = PeriodoRelatorio.deAtividade(periodo.inicio(), periodo.fim());
        List<Venda> vendas = apuracao.vendasConcluidas(efetivo, null);
        BigDecimal faturamentoTotal = soma(vendas, Venda::getValorTotal);

        List<LinhaClienteMaiorVolume> linhas = apuracao.principaisClientes(vendas, limite).stream()
                .map(total -> new LinhaClienteMaiorVolume(
                        total.clienteId(),
                        nomeCliente(total.clienteId()),
                        total.quantidadeVendas(),
                        total.valor(),
                        total.valor().divide(BigDecimal.valueOf(total.quantidadeVendas()), 2, RoundingMode.HALF_UP),
                        faturamentoTotal.signum() == 0
                                ? BigDecimal.ZERO
                                : total.valor().multiply(BigDecimal.valueOf(100))
                                .divide(faturamentoTotal, 1, RoundingMode.HALF_UP)))
                .toList();
        return new RelatorioClientesMaiorVolume(efetivo, limite, faturamentoTotal, linhas);
    }

    private List<PontoVendas> serieDeVendas(PeriodoRelatorio periodo, List<Venda> vendas) {
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
                .map(entrada -> new PontoVendas(entrada.getKey(), quantidades.getOrDefault(entrada.getKey(), 0),
                        entrada.getValue()))
                .toList();
    }

    private String nomeCliente(UUID clienteId) {
        return buscarClientePorIdUseCase.buscarPorId(clienteId).getNomeExibicao();
    }

    private static void validarLimite(int limite) {
        if (limite < 1 || limite > LIMITE_MAXIMO_RANKING) {
            throw new RegraDeNegocioException("O limite deve estar entre 1 e " + LIMITE_MAXIMO_RANKING);
        }
    }
}
