package com.bella.backend.domain.fluxocaixa.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MovimentacaoCaixaTest {

    private static final UUID CATEGORIA_ID = UUID.randomUUID();

    private MovimentacaoCaixa manualPrevista() {
        return MovimentacaoCaixa.novaManual(TipoMovimentacaoCaixa.ENTRADA, "Receita avulsa", CATEGORIA_ID,
                new BigDecimal("500.00"), SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now().plusDays(5), null, null);
    }

    private MovimentacaoCaixa automaticaPrevista(OrigemMovimentacaoCaixa origem) {
        return MovimentacaoCaixa.novaAutomatica(TipoMovimentacaoCaixa.ENTRADA, "Parcela 1", null,
                new BigDecimal("500.00"), SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now().plusDays(5), null,
                origem, UUID.randomUUID(), null);
    }

    @Test
    void criaLancamentoManualPrevisto() {
        MovimentacaoCaixa movimentacao = manualPrevista();

        assertThat(movimentacao.getOrigem()).isEqualTo(OrigemMovimentacaoCaixa.LANCAMENTO_MANUAL);
        assertThat(movimentacao.getOrigemReferenciaId()).isNull();
        assertThat(movimentacao.getSituacao()).isEqualTo(SituacaoMovimentacaoCaixa.PREVISTO);
        assertThat(movimentacao.getDataEfetiva()).isNull();
        assertThat(movimentacao.isCancelada()).isFalse();
    }

    @Test
    void criaLancamentoManualJaRealizadoComDataEfetiva() {
        LocalDate hoje = LocalDate.now();
        MovimentacaoCaixa movimentacao = MovimentacaoCaixa.novaManual(TipoMovimentacaoCaixa.SAIDA, "Despesa",
                CATEGORIA_ID, new BigDecimal("80.00"), SituacaoMovimentacaoCaixa.REALIZADO, hoje, hoje, null);

        assertThat(movimentacao.getDataEfetiva()).isEqualTo(hoje);
        assertThat(movimentacao.getDataReferencia()).isEqualTo(hoje);
    }

    @Test
    void rejeitaValorNaoPositivoETipoAusente() {
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoCaixa.novaManual(TipoMovimentacaoCaixa.ENTRADA,
                "x", CATEGORIA_ID, BigDecimal.ZERO, SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now(), null, null));
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoCaixa.novaManual(null,
                "x", CATEGORIA_ID, BigDecimal.TEN, SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now(), null, null));
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoCaixa.novaManual(TipoMovimentacaoCaixa.ENTRADA,
                " ", CATEGORIA_ID, BigDecimal.TEN, SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now(), null, null));
    }

    @Test
    void realizadoExigeDataDeRealizacaoEPrevistoNaoPodeTer() {
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoCaixa.novaManual(TipoMovimentacaoCaixa.ENTRADA,
                "x", CATEGORIA_ID, BigDecimal.TEN, SituacaoMovimentacaoCaixa.REALIZADO, LocalDate.now(), null, null));
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoCaixa.novaManual(TipoMovimentacaoCaixa.ENTRADA,
                "x", CATEGORIA_ID, BigDecimal.TEN, SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now(),
                LocalDate.now(), null));
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoCaixa.novaManual(TipoMovimentacaoCaixa.ENTRADA,
                "x", CATEGORIA_ID, BigDecimal.TEN, SituacaoMovimentacaoCaixa.REALIZADO, LocalDate.now(),
                LocalDate.now().plusDays(1), null));
    }

    @Test
    void lancamentoManualExigeCategoria() {
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoCaixa.novaManual(TipoMovimentacaoCaixa.ENTRADA,
                "x", null, BigDecimal.TEN, SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now(), null, null));
    }

    @Test
    void movimentacaoAutomaticaExigeReferenciaEOrigemNaoManual() {
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoCaixa.novaAutomatica(
                TipoMovimentacaoCaixa.ENTRADA, "x", null, BigDecimal.TEN, SituacaoMovimentacaoCaixa.PREVISTO,
                LocalDate.now(), null, OrigemMovimentacaoCaixa.CONTA_RECEBER, null, null));
        assertThrows(RegraDeNegocioException.class, () -> MovimentacaoCaixa.novaAutomatica(
                TipoMovimentacaoCaixa.ENTRADA, "x", null, BigDecimal.TEN, SituacaoMovimentacaoCaixa.PREVISTO,
                LocalDate.now(), null, OrigemMovimentacaoCaixa.LANCAMENTO_MANUAL, UUID.randomUUID(), null));
    }

    @Test
    void realizarTransformaAPrevisaoNoMesmoRegistro() {
        MovimentacaoCaixa movimentacao = automaticaPrevista(OrigemMovimentacaoCaixa.CONTA_RECEBER);
        UUID id = movimentacao.getId();

        movimentacao.realizar(null);

        assertThat(movimentacao.getId()).isEqualTo(id);
        assertThat(movimentacao.getSituacao()).isEqualTo(SituacaoMovimentacaoCaixa.REALIZADO);
        assertThat(movimentacao.getDataEfetiva()).isEqualTo(LocalDate.now());
        assertThrows(RegraDeNegocioException.class, () -> movimentacao.realizar(LocalDate.now()));
    }

    @Test
    void editaLancamentoManualPrevistoMasNaoOsRealizadosNemOsAutomaticos() {
        MovimentacaoCaixa movimentacao = manualPrevista();
        movimentacao.editarManual(TipoMovimentacaoCaixa.SAIDA, "Ajustada", CATEGORIA_ID, new BigDecimal("90.00"),
                LocalDate.now().plusDays(9), "obs");
        assertThat(movimentacao.getTipo()).isEqualTo(TipoMovimentacaoCaixa.SAIDA);
        assertThat(movimentacao.getValor()).isEqualByComparingTo("90.00");

        movimentacao.realizar(LocalDate.now());
        assertThrows(RegraDeNegocioException.class, () -> movimentacao.editarManual(TipoMovimentacaoCaixa.SAIDA,
                "x", CATEGORIA_ID, BigDecimal.TEN, LocalDate.now(), null));

        MovimentacaoCaixa automatica = automaticaPrevista(OrigemMovimentacaoCaixa.CONTA_PAGAR);
        assertThrows(RegraDeNegocioException.class, () -> automatica.editarManual(TipoMovimentacaoCaixa.SAIDA,
                "x", CATEGORIA_ID, BigDecimal.TEN, LocalDate.now(), null));
    }

    @Test
    void cancelamentoManualExigeMotivoEPermaneceRastreavel() {
        MovimentacaoCaixa movimentacao = manualPrevista();
        movimentacao.realizar(LocalDate.now());

        assertThrows(RegraDeNegocioException.class, () -> movimentacao.cancelarManual(" "));
        movimentacao.cancelarManual("Lançamento incorreto");

        assertThat(movimentacao.isCancelada()).isTrue();
        assertThat(movimentacao.getMotivoCancelamento()).isEqualTo("Lançamento incorreto");
        assertThat(movimentacao.getSituacao()).isEqualTo(SituacaoMovimentacaoCaixa.REALIZADO);
        assertThat(movimentacao.getDataEfetiva()).isNotNull();
        assertThrows(RegraDeNegocioException.class, () -> movimentacao.cancelarManual("De novo"));
        assertThrows(RegraDeNegocioException.class, () -> movimentacao.realizar(LocalDate.now()));
    }

    @Test
    void movimentacaoAutomaticaNaoPodeSerCanceladaManualmenteMasPodePelaOrigem() {
        MovimentacaoCaixa automatica = automaticaPrevista(OrigemMovimentacaoCaixa.CONTA_RECEBER);

        assertThrows(RegraDeNegocioException.class, () -> automatica.cancelarManual("Motivo"));
        automatica.cancelarPorOrigem("Conta cancelada");
        assertThat(automatica.isCancelada()).isTrue();

        assertThrows(RegraDeNegocioException.class, () -> manualPrevista().cancelarPorOrigem("Motivo"));
    }
}
