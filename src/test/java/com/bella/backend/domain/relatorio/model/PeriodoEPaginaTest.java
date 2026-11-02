package com.bella.backend.domain.relatorio.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodoEPaginaTest {

    @Test
    void periodoDeAtividadeSemDatasUsaOMesCorrente() {
        PeriodoRelatorio periodo = PeriodoRelatorio.deAtividade(null, null);

        LocalDate hoje = LocalDate.now();
        assertThat(periodo.inicio()).isEqualTo(hoje.withDayOfMonth(1));
        assertThat(periodo.fim()).isEqualTo(hoje.withDayOfMonth(hoje.lengthOfMonth()));
    }

    @Test
    void periodoDeAtividadeComSoInicioVaiAteOFimDoMesDoInicio() {
        PeriodoRelatorio periodo = PeriodoRelatorio.deAtividade(LocalDate.of(2026, 2, 10), null);

        assertThat(periodo.fim()).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void periodoOpcionalAceitaPontasAusentesMasRejeitaFimAnteriorAoInicio() {
        PeriodoRelatorio aberto = PeriodoRelatorio.opcional(null, null);

        assertThat(aberto.contem(LocalDate.of(1990, 1, 1))).isTrue();
        assertThatThrownBy(() -> PeriodoRelatorio.opcional(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 1)))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void periodoEInclusivoNasDuasPontas() {
        PeriodoRelatorio periodo = PeriodoRelatorio.opcional(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(periodo.contem(LocalDate.of(2026, 3, 1))).isTrue();
        assertThat(periodo.contem(LocalDate.of(2026, 3, 31))).isTrue();
        assertThat(periodo.contem(LocalDate.of(2026, 4, 1))).isFalse();
        assertThat(periodo.contem(LocalDate.of(2026, 2, 28))).isFalse();
    }

    @Test
    void serieEDiariaAte62DiasEMensalDepois() {
        PeriodoRelatorio curto = PeriodoRelatorio.opcional(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));
        PeriodoRelatorio longo = PeriodoRelatorio.opcional(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 6, 30));

        assertThat(curto.granularidade()).isEqualTo(Granularidade.DIA);
        assertThat(curto.chaves()).hasSize(10);
        assertThat(longo.granularidade()).isEqualTo(Granularidade.MES);
        assertThat(longo.chaves()).hasSize(6).first().isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(longo.chave(LocalDate.of(2026, 3, 20))).isEqualTo(LocalDate.of(2026, 3, 1));
    }

    @Test
    void serieExigePeriodoComAsDuasPontas() {
        assertThatThrownBy(() -> PeriodoRelatorio.opcional(LocalDate.of(2026, 1, 1), null).chaves())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paginaDividePelosParametrosEInformaOTotal() {
        List<Integer> todos = IntStream.rangeClosed(1, 45).boxed().toList();

        Pagina<Integer> segunda = Pagina.de(todos, 1, 20);
        Pagina<Integer> ultima = Pagina.de(todos, 2, 20);
        Pagina<Integer> alemDoFim = Pagina.de(todos, 9, 20);

        assertThat(segunda.itens()).hasSize(20).first().isEqualTo(21);
        assertThat(segunda.totalItens()).isEqualTo(45);
        assertThat(segunda.totalPaginas()).isEqualTo(3);
        assertThat(ultima.itens()).hasSize(5);
        assertThat(alemDoFim.itens()).isEmpty();
        assertThat(Pagina.de(List.<Integer>of(), 0, 20).totalPaginas()).isZero();
    }

    @Test
    void paginaRejeitaParametrosInvalidos() {
        assertThatThrownBy(() -> Pagina.validar(-1, 20)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Pagina.validar(0, 0)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Pagina.validar(0, Pagina.TAMANHO_MAXIMO + 1))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
