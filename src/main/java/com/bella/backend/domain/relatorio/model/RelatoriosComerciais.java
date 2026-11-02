package com.bella.backend.domain.relatorio.model;

import com.bella.backend.domain.orcamento.model.StatusOrcamento;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.venda.model.FormaPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Relatórios comerciais. São consultas sobre vendas, pedidos e orçamentos já existentes; nada é
 * gravado. Vendas e pedidos cancelados ficam fora dos totais.
 */
public final class RelatoriosComerciais {

    private RelatoriosComerciais() {
    }

    public record ResumoVendas(int quantidade, BigDecimal faturamento, BigDecimal ticketMedio,
                               BigDecimal descontoTotal, BigDecimal freteTotal) {
    }

    public record PontoVendas(LocalDate data, int quantidade, BigDecimal valor) {
    }

    public record LinhaVenda(UUID id, LocalDate data, UUID clienteId, String clienteNome,
                             FormaPagamento formaPagamento, BigDecimal valorDesconto, BigDecimal valorFrete,
                             BigDecimal valorTotal) {
    }

    public record RelatorioVendas(PeriodoRelatorio periodo, Granularidade granularidade, ResumoVendas resumo,
                                  List<PontoVendas> serie, Pagina<LinhaVenda> vendas) {
    }

    public record LinhaPedidosPorStatus(StatusPedido status, int quantidade, BigDecimal valorTotal) {
    }

    /** {@code totalValidos} soma todos os status, exceto os pedidos cancelados. */
    public record RelatorioPedidosPorStatus(PeriodoRelatorio periodo, List<LinhaPedidosPorStatus> porStatus,
                                            int quantidadeValidos, BigDecimal valorValidos) {
    }

    public record LinhaOrcamentosPorStatus(StatusOrcamento status, int quantidade, BigDecimal valorTotal) {
    }

    /**
     * Orçamentos emitidos são os que saíram do rascunho e não foram cancelados (enviados, aprovados,
     * recusados ou expirados). As taxas são percentuais (0 a 100), nulas sem orçamentos emitidos:
     * aprovação = aprovados / emitidos; conversão em pedido = emitidos que geraram algum pedido não
     * cancelado / emitidos; conversão em venda = emitidos cujo pedido virou venda concluída / emitidos.
     */
    public record RelatorioOrcamentos(PeriodoRelatorio periodo, List<LinhaOrcamentosPorStatus> porStatus,
                                      int emitidos, BigDecimal valorEmitidos, int aprovados, int recusados,
                                      BigDecimal taxaAprovacao, BigDecimal taxaConversaoPedidos,
                                      BigDecimal taxaConversaoVendas) {
    }

    public record LinhaProdutoMaisVendido(UUID produtoId, String codigoInterno, String nome, UUID categoriaId,
                                          String categoriaNome, BigDecimal quantidade, BigDecimal valor,
                                          int quantidadeVendas) {
    }

    public record RelatorioProdutosMaisVendidos(PeriodoRelatorio periodo, int limite,
                                                List<LinhaProdutoMaisVendido> produtos) {
    }

    public record LinhaClienteMaiorVolume(UUID clienteId, String nome, int quantidadeVendas, BigDecimal valor,
                                          BigDecimal ticketMedio, BigDecimal participacaoPercentual) {
    }

    /** A participação é calculada sobre o faturamento total do período (todos os clientes). */
    public record RelatorioClientesMaiorVolume(PeriodoRelatorio periodo, int limite, BigDecimal faturamentoTotal,
                                               List<LinhaClienteMaiorVolume> clientes) {
    }
}
