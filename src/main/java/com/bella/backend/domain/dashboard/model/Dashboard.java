package com.bella.backend.domain.dashboard.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Visão consolidada de indicadores dos módulos existentes. Não é um agregado nem guarda estado: é
 * montada sob demanda, reaproveitando as consultas e os cálculos de cada módulo (contas, fluxo de
 * caixa, estoque, vendas), sem reimplementar suas regras.
 */
public record Dashboard(
        Periodo periodo,
        Comercial comercial,
        Financeiro financeiro,
        List<ProdutoAbaixoDoMinimo> produtosAbaixoDoMinimo,
        int totalProdutosAbaixoDoMinimo,
        List<ProdutoMaisVendido> produtosMaisVendidos,
        List<ClienteDestaque> principaisClientes
) {

    public enum Granularidade {
        DIA,
        MES
    }

    public record Periodo(LocalDate inicio, LocalDate fim, Granularidade granularidade) {
    }

    /**
     * Vendas e pedidos desconsideram registros cancelados. Orçamentos emitidos são os que saíram do
     * rascunho e não foram cancelados (enviados, aprovados, recusados ou expirados). As taxas de
     * conversão são percentuais (0 a 100) e ficam nulas quando não há orçamentos emitidos.
     */
    public record Comercial(
            BigDecimal faturamento,
            int quantidadeVendas,
            BigDecimal ticketMedio,
            int quantidadePedidos,
            int quantidadePedidosCancelados,
            int orcamentosEmitidos,
            BigDecimal valorOrcamentosEmitidos,
            BigDecimal taxaConversaoPedidos,
            BigDecimal taxaConversaoVendas,
            List<PontoVendas> vendasPorPeriodo
    ) {
    }

    public record PontoVendas(LocalDate data, BigDecimal valor, int quantidade) {
    }

    /**
     * Posição das contas (fotografia de hoje, não depende do período), exceto
     * {@code vencendoNoPeriodo}, que soma o saldo das parcelas com vencimento dentro do período.
     */
    public record PosicaoContas(
            BigDecimal emAberto,
            BigDecimal vencido,
            BigDecimal aVencer,
            BigDecimal vencendoNoPeriodo,
            int quantidadeContasEmAberto,
            int quantidadeContasVencidas
    ) {
    }

    public record Financeiro(
            PosicaoContas contasReceber,
            PosicaoContas contasPagar,
            BigDecimal saldoAtual,
            BigDecimal entradasPeriodo,
            BigDecimal saidasPeriodo,
            BigDecimal saldoPeriodo,
            BigDecimal entradasPrevistasPeriodo,
            BigDecimal saidasPrevistasPeriodo,
            List<PontoFluxo> fluxoPorPeriodo
    ) {
    }

    public record PontoFluxo(LocalDate data, BigDecimal entradas, BigDecimal saidas) {
    }

    public record ProdutoAbaixoDoMinimo(
            UUID produtoId,
            String nome,
            BigDecimal quantidadeDisponivel,
            BigDecimal estoqueMinimo
    ) {
    }

    public record ProdutoMaisVendido(UUID produtoId, String nome, BigDecimal quantidade, BigDecimal valor) {
    }

    public record ClienteDestaque(UUID clienteId, String nome, int quantidadeVendas, BigDecimal valor) {
    }
}
