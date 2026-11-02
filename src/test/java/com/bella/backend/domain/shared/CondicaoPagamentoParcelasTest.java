package com.bella.backend.domain.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CondicaoPagamentoParcelasTest {

    private static final LocalDate BASE = LocalDate.of(2026, 1, 31);

    private List<CondicaoPagamentoParcelas.Parcela> gerar(String condicao, String valor) {
        return CondicaoPagamentoParcelas.gerar(condicao, new BigDecimal(valor), BASE);
    }

    @Test
    void aVistaGeraUmaParcelaNaDataBase() {
        List<CondicaoPagamentoParcelas.Parcela> parcelas = gerar("À vista", "100.00");

        assertThat(parcelas).hasSize(1);
        assertThat(parcelas.get(0).vencimento()).isEqualTo(BASE);
        assertThat(parcelas.get(0).valor()).isEqualByComparingTo("100.00");
    }

    @Test
    void textoNaoReconhecidoOuVazioTrataComoAVista() {
        assertThat(gerar("Boleto", "10.00")).extracting(CondicaoPagamentoParcelas.Parcela::vencimento)
                .containsExactly(BASE);
        assertThat(gerar(null, "10.00")).hasSize(1);
        assertThat(gerar("  ", "10.00")).hasSize(1);
    }

    @Test
    void prazosEmDiasGeramUmaParcelaPorPrazo() {
        List<CondicaoPagamentoParcelas.Parcela> parcelas = gerar("30/60/90 dias", "100.00");

        assertThat(parcelas).extracting(CondicaoPagamentoParcelas.Parcela::vencimento)
                .containsExactly(BASE.plusDays(30), BASE.plusDays(60), BASE.plusDays(90));
        assertThat(parcelas).extracting(CondicaoPagamentoParcelas.Parcela::valor)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(new BigDecimal("33.33"), new BigDecimal("33.33"), new BigDecimal("33.34"));
    }

    @Test
    void umPrazoUnicoEmDiasGeraUmaParcela() {
        assertThat(gerar("30 dias", "57.00")).extracting(CondicaoPagamentoParcelas.Parcela::vencimento)
                .containsExactly(BASE.plusDays(30));
    }

    @Test
    void parcelasMensaisAjustamOFimDoMes() {
        List<CondicaoPagamentoParcelas.Parcela> parcelas = gerar("3x no cartão", "90.00");

        assertThat(parcelas).extracting(CondicaoPagamentoParcelas.Parcela::vencimento)
                .containsExactly(LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 31), LocalDate.of(2026, 4, 30));
    }

    @Test
    void reconheceVezesEParcelasPorExtenso() {
        assertThat(gerar("em 4 vezes", "100.00")).hasSize(4);
        assertThat(gerar("2 parcelas", "100.00")).hasSize(2);
    }

    @Test
    void somaDasParcelasSempreIgualAoTotal() {
        BigDecimal soma = gerar("7x", "100.01").stream()
                .map(CondicaoPagamentoParcelas.Parcela::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(soma).isEqualByComparingTo("100.01");
    }
}
