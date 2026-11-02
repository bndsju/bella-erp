package com.bella.backend.domain.relatorio.model;

import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Relatórios financeiros. Contas canceladas ficam fora dos totais; vencida não é status, é calculada
 * pela data das parcelas com saldo. No fluxo de caixa, previsto e realizado nunca se misturam.
 */
public final class RelatoriosFinanceiros {

    private RelatoriosFinanceiros() {
    }

    public enum TipoTitulo {
        RECEBER,
        PAGAR
    }

    /** Totais de contas a receber ou a pagar: o saldo é o que ainda falta liquidar. */
    public record TotaisContas(int quantidadeContas, BigDecimal valorTotal, BigDecimal valorLiquidado,
                               BigDecimal saldoPendente, BigDecimal saldoVencido, BigDecimal saldoAVencer) {
    }

    public record LinhaConta(UUID id, String descricao, UUID parceiroId, String parceiroNome, String categoriaNome,
                             BigDecimal valorTotal, BigDecimal valorLiquidado, BigDecimal saldoPendente,
                             LocalDate proximoVencimento, String status, boolean vencida) {
    }

    public record RelatorioContas(PeriodoRelatorio vencimento, TotaisContas totais, Pagina<LinhaConta> contas) {
    }

    public record LinhaTitulo(TipoTitulo tipo, UUID contaId, String descricao, String parceiroNome, int parcela,
                              LocalDate vencimento, BigDecimal saldoPendente, long diasEmAtraso) {
    }

    /** Faixa de vencimento por dias de atraso (ex.: "1 a 30 dias vencido", "A vencer em até 7 dias"). */
    public record FaixaVencimento(String faixa, BigDecimal receber, BigDecimal pagar) {
    }

    public record TotaisTitulos(BigDecimal vencidoReceber, BigDecimal aVencerReceber, BigDecimal vencidoPagar,
                                BigDecimal aVencerPagar) {
    }

    public record RelatorioVencimentos(LocalDate referencia, PeriodoRelatorio vencimento, TotaisTitulos totais,
                                       List<FaixaVencimento> faixas, Pagina<LinhaTitulo> titulos) {
    }

    public record PontoFluxo(LocalDate data, BigDecimal entradas, BigDecimal saidas) {
    }

    public record ResumoFluxo(BigDecimal entradasPrevistas, BigDecimal saidasPrevistas,
                              BigDecimal entradasRealizadas, BigDecimal saidasRealizadas,
                              BigDecimal saldoRealizado, BigDecimal saldoProjetado) {
    }

    public record FluxoPorCategoria(UUID categoriaId, String categoriaNome, ResumoFluxo resumo) {
    }

    public record LinhaMovimentacaoCaixa(UUID id, LocalDate data, TipoMovimentacaoCaixa tipo, String descricao,
                                         String categoriaNome, OrigemMovimentacaoCaixa origem,
                                         SituacaoMovimentacaoCaixa situacao, BigDecimal valor) {
    }

    /**
     * {@code saldoAcumuladoAteFim} considera tudo o que foi realizado até o fim do período, sem os
     * demais filtros; o resumo e as séries respeitam todos os filtros informados.
     */
    public record RelatorioFluxoCaixa(PeriodoRelatorio periodo, Granularidade granularidade, ResumoFluxo resumo,
                                      BigDecimal saldoAcumuladoAteFim, List<PontoFluxo> serie,
                                      List<FluxoPorCategoria> porCategoria,
                                      Pagina<LinhaMovimentacaoCaixa> movimentacoes) {
    }
}
