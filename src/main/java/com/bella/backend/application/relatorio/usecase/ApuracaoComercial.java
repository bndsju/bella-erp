package com.bella.backend.application.relatorio.usecase;

import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.StatusOrcamento;
import com.bella.backend.domain.orcamento.port.in.ListarOrcamentosUseCase;
import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.pedido.port.in.ListarPedidosUseCase;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.venda.model.StatusVenda;
import com.bella.backend.domain.venda.model.Venda;
import com.bella.backend.domain.venda.model.VendaItem;
import com.bella.backend.domain.venda.port.in.ListarVendasUseCase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Regras de apuração comercial compartilhadas pelo dashboard e pelos relatórios, para que os dois
 * sempre mostrem os mesmos números: quais vendas contam, como se ranqueia e o que é conversão.
 */
@Component
public class ApuracaoComercial {

    private static final Set<StatusOrcamento> STATUS_EMITIDOS = Set.of(
            StatusOrcamento.ENVIADO, StatusOrcamento.APROVADO, StatusOrcamento.RECUSADO, StatusOrcamento.EXPIRADO);

    private final ListarVendasUseCase listarVendasUseCase;
    private final ListarPedidosUseCase listarPedidosUseCase;
    private final ListarOrcamentosUseCase listarOrcamentosUseCase;

    public ApuracaoComercial(ListarVendasUseCase listarVendasUseCase, ListarPedidosUseCase listarPedidosUseCase,
                             ListarOrcamentosUseCase listarOrcamentosUseCase) {
        this.listarVendasUseCase = listarVendasUseCase;
        this.listarPedidosUseCase = listarPedidosUseCase;
        this.listarOrcamentosUseCase = listarOrcamentosUseCase;
    }

    public record TotalPorProduto(UUID produtoId, BigDecimal quantidade, BigDecimal valor, int quantidadeVendas) {
    }

    public record TotalPorCliente(UUID clienteId, int quantidadeVendas, BigDecimal valor) {
    }

    /** Orçamentos emitidos e quantos deles viraram pedido e venda (contagens, sem percentual). */
    public record Conversao(List<Orcamento> emitidos, long convertidosEmPedido, long convertidosEmVenda) {

        public BigDecimal valorEmitidos() {
            return soma(emitidos, Orcamento::getValorTotal);
        }

        public BigDecimal taxaConversaoPedidos() {
            return percentual(convertidosEmPedido, emitidos.size());
        }

        public BigDecimal taxaConversaoVendas() {
            return percentual(convertidosEmVenda, emitidos.size());
        }
    }

    /** Vendas concluídas (as canceladas nunca contam) no período, com filtro opcional de cliente. */
    public List<Venda> vendasConcluidas(PeriodoRelatorio periodo, UUID clienteId) {
        return listarVendasUseCase.listar(new ListarVendasUseCase.FiltroListagem(clienteId, StatusVenda.CONCLUIDA))
                .stream()
                .filter(venda -> periodo.contem(venda.getCriadoEm()))
                .toList();
    }

    public List<Pedido> pedidos(PeriodoRelatorio periodo, UUID clienteId) {
        return listarPedidosUseCase.listar(new ListarPedidosUseCase.FiltroListagem(clienteId, null)).stream()
                .filter(pedido -> periodo.contem(pedido.getCriadoEm()))
                .toList();
    }

    public List<Orcamento> orcamentos(PeriodoRelatorio periodo, UUID clienteId) {
        return listarOrcamentosUseCase.listar(new ListarOrcamentosUseCase.FiltroListagem(clienteId, null)).stream()
                .filter(orcamento -> periodo.contem(orcamento.getCriadoEm()))
                .toList();
    }

    /** Totais por produto, do maior para o menor em quantidade; {@code aceitaProduto} filtra os itens. */
    public List<TotalPorProduto> produtosMaisVendidos(List<Venda> vendas, Predicate<UUID> aceitaProduto,
                                                      int limite) {
        Map<UUID, BigDecimal> quantidades = new HashMap<>();
        Map<UUID, BigDecimal> valores = new HashMap<>();
        Map<UUID, Set<UUID>> vendasPorProduto = new HashMap<>();
        for (Venda venda : vendas) {
            for (VendaItem item : venda.getItens()) {
                if (!aceitaProduto.test(item.getProdutoId())) {
                    continue;
                }
                quantidades.merge(item.getProdutoId(), item.getQuantidade(), BigDecimal::add);
                valores.merge(item.getProdutoId(), item.getSubtotal(), BigDecimal::add);
                vendasPorProduto.computeIfAbsent(item.getProdutoId(), chave -> new HashSet<>()).add(venda.getId());
            }
        }
        return quantidades.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .limit(limite)
                .map(entrada -> new TotalPorProduto(entrada.getKey(), entrada.getValue(),
                        valores.get(entrada.getKey()), vendasPorProduto.get(entrada.getKey()).size()))
                .toList();
    }

    /** Totais por cliente, do maior para o menor valor comprado. */
    public List<TotalPorCliente> principaisClientes(List<Venda> vendas, int limite) {
        Map<UUID, BigDecimal> valores = new HashMap<>();
        Map<UUID, Integer> quantidades = new HashMap<>();
        for (Venda venda : vendas) {
            valores.merge(venda.getClienteId(), venda.getValorTotal(), BigDecimal::add);
            quantidades.merge(venda.getClienteId(), 1, Integer::sum);
        }
        return valores.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .limit(limite)
                .map(entrada -> new TotalPorCliente(entrada.getKey(), quantidades.get(entrada.getKey()),
                        entrada.getValue()))
                .toList();
    }

    public Conversao conversao(PeriodoRelatorio periodo, UUID clienteId) {
        List<Orcamento> emitidos = orcamentos(periodo, clienteId).stream()
                .filter(orcamento -> STATUS_EMITIDOS.contains(orcamento.getStatus()))
                .toList();

        List<Pedido> todosPedidos = listarPedidosUseCase.listar(new ListarPedidosUseCase.FiltroListagem(null, null));
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
        return new Conversao(emitidos, convertidosEmPedido, convertidosEmVenda);
    }

    public static <T> BigDecimal soma(List<T> itens, Function<T, BigDecimal> valor) {
        return itens.stream().map(valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Percentual com uma casa decimal; nulo quando não há base de cálculo. */
    public static BigDecimal percentual(long parte, long total) {
        if (total == 0) {
            return null;
        }
        return BigDecimal.valueOf(parte * 100L).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }
}
