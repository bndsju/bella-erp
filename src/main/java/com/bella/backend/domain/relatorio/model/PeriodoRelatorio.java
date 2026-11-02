package com.bella.backend.domain.relatorio.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Período de um relatório, sempre inclusivo nas duas pontas. Relatórios de atividade (vendas,
 * pedidos, movimentações...) usam {@link #deAtividade}, que assume o mês corrente quando nada é
 * informado; relatórios de posição (contas, vencimentos) usam {@link #opcional}, em que cada ponta
 * ausente significa "sem limite". Em ambos, fim anterior ao início é rejeitado.
 */
public record PeriodoRelatorio(LocalDate inicio, LocalDate fim) {

    private static final long LIMITE_DIAS_SERIE_DIARIA = 62;

    public PeriodoRelatorio {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new RegraDeNegocioException("A data final do período não pode ser anterior à data inicial");
        }
    }

    public static PeriodoRelatorio deAtividade(LocalDate inicio, LocalDate fim) {
        LocalDate de = inicio != null ? inicio : LocalDate.now().withDayOfMonth(1);
        LocalDate ate = fim != null ? fim : de.withDayOfMonth(de.lengthOfMonth());
        return new PeriodoRelatorio(de, ate);
    }

    public static PeriodoRelatorio opcional(LocalDate inicio, LocalDate fim) {
        return new PeriodoRelatorio(inicio, fim);
    }

    public boolean contem(LocalDate data) {
        return (inicio == null || !data.isBefore(inicio)) && (fim == null || !data.isAfter(fim));
    }

    public boolean contem(Instant instante) {
        return contem(instante.atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /** Séries só existem para períodos com as duas pontas; acima de 62 dias agrupam por mês. */
    public Granularidade granularidade() {
        exigirLimitado();
        return inicio.plusDays(LIMITE_DIAS_SERIE_DIARIA).isBefore(fim) ? Granularidade.MES : Granularidade.DIA;
    }

    /** Início do intervalo (dia ou primeiro dia do mês) em que a data cai na série. */
    public LocalDate chave(LocalDate data) {
        return granularidade() == Granularidade.MES ? data.withDayOfMonth(1) : data;
    }

    /** Todos os intervalos do período, inclusive os sem movimento, para a série não ter buracos. */
    public List<LocalDate> chaves() {
        Granularidade granularidade = granularidade();
        List<LocalDate> chaves = new ArrayList<>();
        LocalDate atual = chave(inicio);
        while (!atual.isAfter(fim)) {
            chaves.add(atual);
            atual = granularidade == Granularidade.MES ? atual.plusMonths(1) : atual.plusDays(1);
        }
        return chaves;
    }

    public Instant inicioComoInstante() {
        return inicio == null ? null : inicio.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    /** Fim inclusivo: o instante seguinte ao último dia do período. */
    public Instant fimComoInstante() {
        return fim == null ? null : fim.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private void exigirLimitado() {
        if (inicio == null || fim == null) {
            throw new IllegalStateException("Série temporal exige período com início e fim");
        }
    }
}
