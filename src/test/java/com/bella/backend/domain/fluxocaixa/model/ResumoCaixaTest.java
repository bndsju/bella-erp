package com.bella.backend.domain.fluxocaixa.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResumoCaixaTest {

    private MovimentacaoCaixa mov(TipoMovimentacaoCaixa tipo, String valor, SituacaoMovimentacaoCaixa situacao) {
        LocalDate hoje = LocalDate.now();
        return MovimentacaoCaixa.novaAutomatica(tipo, "Mov", null, new BigDecimal(valor), situacao, hoje,
                situacao == SituacaoMovimentacaoCaixa.REALIZADO ? hoje : null,
                tipo == TipoMovimentacaoCaixa.ENTRADA ? OrigemMovimentacaoCaixa.CONTA_RECEBER
                        : OrigemMovimentacaoCaixa.CONTA_PAGAR,
                UUID.randomUUID(), null);
    }

    @Test
    void calculaTotaisESaldos() {
        ResumoCaixa resumo = ResumoCaixa.de(List.of(
                mov(TipoMovimentacaoCaixa.ENTRADA, "1000.00", SituacaoMovimentacaoCaixa.REALIZADO),
                mov(TipoMovimentacaoCaixa.SAIDA, "300.00", SituacaoMovimentacaoCaixa.REALIZADO),
                mov(TipoMovimentacaoCaixa.ENTRADA, "500.00", SituacaoMovimentacaoCaixa.PREVISTO),
                mov(TipoMovimentacaoCaixa.SAIDA, "200.00", SituacaoMovimentacaoCaixa.PREVISTO)));

        assertThat(resumo.entradasRealizadas()).isEqualByComparingTo("1000.00");
        assertThat(resumo.saidasRealizadas()).isEqualByComparingTo("300.00");
        assertThat(resumo.entradasPrevistas()).isEqualByComparingTo("500.00");
        assertThat(resumo.saidasPrevistas()).isEqualByComparingTo("200.00");
        assertThat(resumo.saldoRealizado()).isEqualByComparingTo("700.00");
        assertThat(resumo.saldoProjetado()).isEqualByComparingTo("1000.00");
    }

    @Test
    void previstoNaoAlteraSaldoRealizado() {
        ResumoCaixa resumo = ResumoCaixa.de(List.of(
                mov(TipoMovimentacaoCaixa.ENTRADA, "500.00", SituacaoMovimentacaoCaixa.PREVISTO)));

        assertThat(resumo.saldoRealizado()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.saldoProjetado()).isEqualByComparingTo("500.00");
    }

    @Test
    void realizarAParcelaNaoDobraOSaldo() {
        MovimentacaoCaixa parcela = mov(TipoMovimentacaoCaixa.ENTRADA, "500.00", SituacaoMovimentacaoCaixa.PREVISTO);
        assertThat(ResumoCaixa.de(List.of(parcela)).saldoProjetado()).isEqualByComparingTo("500.00");

        parcela.realizar(LocalDate.now());
        ResumoCaixa resumo = ResumoCaixa.de(List.of(parcela));

        assertThat(resumo.entradasPrevistas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.entradasRealizadas()).isEqualByComparingTo("500.00");
        assertThat(resumo.saldoRealizado()).isEqualByComparingTo("500.00");
        assertThat(resumo.saldoProjetado()).isEqualByComparingTo("500.00");
    }

    @Test
    void entradasAumentamESaidasDiminuemOSaldo() {
        ResumoCaixa soSaida = ResumoCaixa.de(List.of(
                mov(TipoMovimentacaoCaixa.SAIDA, "120.00", SituacaoMovimentacaoCaixa.REALIZADO)));

        assertThat(soSaida.saldoRealizado()).isEqualByComparingTo("-120.00");
    }

    @Test
    void movimentacoesCanceladasNaoEntramNasSomas() {
        MovimentacaoCaixa cancelada = mov(TipoMovimentacaoCaixa.ENTRADA, "999.00",
                SituacaoMovimentacaoCaixa.PREVISTO);
        cancelada.cancelarPorOrigem("Conta cancelada");

        ResumoCaixa resumo = ResumoCaixa.de(List.of(cancelada,
                mov(TipoMovimentacaoCaixa.ENTRADA, "10.00", SituacaoMovimentacaoCaixa.REALIZADO)));

        assertThat(resumo.entradasPrevistas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.saldoProjetado()).isEqualByComparingTo("10.00");
    }

    @Test
    void semMovimentacoesTudoZero() {
        ResumoCaixa resumo = ResumoCaixa.de(List.of());

        assertThat(resumo.saldoRealizado()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.saldoProjetado()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
