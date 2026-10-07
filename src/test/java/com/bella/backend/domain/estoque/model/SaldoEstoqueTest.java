package com.bella.backend.domain.estoque.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaldoEstoqueTest {

    private final UUID produtoId = UUID.randomUUID();

    @Test
    void novoSaldoComecaZerado() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);

        assertThat(saldo.getEstoqueFisico()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saldo.getQuantidadeReservada()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saldo.getDisponivel()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void entradaAumentaEstoqueFisico() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);

        saldo.registrarEntrada(new BigDecimal("10"));

        assertThat(saldo.getEstoqueFisico()).isEqualByComparingTo("10");
        assertThat(saldo.getDisponivel()).isEqualByComparingTo("10");
    }

    @Test
    void saidaDiminuiEstoqueFisico() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);
        saldo.registrarEntrada(new BigDecimal("10"));

        saldo.registrarSaida(new BigDecimal("4"));

        assertThat(saldo.getEstoqueFisico()).isEqualByComparingTo("6");
    }

    @Test
    void saidaRejeitaQuandoFicariaNegativa() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);
        saldo.registrarEntrada(new BigDecimal("5"));

        assertThrows(RegraDeNegocioException.class, () -> saldo.registrarSaida(new BigDecimal("6")));
    }

    @Test
    void saidaRejeitaQuandoConsumiriaEstoqueReservado() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);
        saldo.registrarEntrada(new BigDecimal("10"));
        saldo.reservar(new BigDecimal("3"));

        assertThrows(RegraDeNegocioException.class, () -> saldo.registrarSaida(new BigDecimal("8")));
        assertThat(saldo.getEstoqueFisico()).isEqualByComparingTo("10");
    }

    @Test
    void reservaReduzDisponivelSemAlterarFisico() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);
        saldo.registrarEntrada(new BigDecimal("10"));

        saldo.reservar(new BigDecimal("3"));

        assertThat(saldo.getEstoqueFisico()).isEqualByComparingTo("10");
        assertThat(saldo.getQuantidadeReservada()).isEqualByComparingTo("3");
        assertThat(saldo.getDisponivel()).isEqualByComparingTo("7");
    }

    @Test
    void reservaRejeitaQuandoSuperaDisponivel() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);
        saldo.registrarEntrada(new BigDecimal("5"));

        assertThrows(RegraDeNegocioException.class, () -> saldo.reservar(new BigDecimal("6")));
    }

    @Test
    void liberacaoDeReservaDevolveDisponivelSemAlterarFisico() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);
        saldo.registrarEntrada(new BigDecimal("10"));
        saldo.reservar(new BigDecimal("3"));

        saldo.liberarReserva(new BigDecimal("3"));

        assertThat(saldo.getEstoqueFisico()).isEqualByComparingTo("10");
        assertThat(saldo.getQuantidadeReservada()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saldo.getDisponivel()).isEqualByComparingTo("10");
    }

    @Test
    void liberacaoDeReservaRejeitaQuandoSuperaReservado() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);
        saldo.registrarEntrada(new BigDecimal("10"));
        saldo.reservar(new BigDecimal("3"));

        assertThrows(RegraDeNegocioException.class, () -> saldo.liberarReserva(new BigDecimal("4")));
    }

    @Test
    void consumoDeReservaReduzFisicoEReservadoJuntos() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);
        saldo.registrarEntrada(new BigDecimal("10"));
        saldo.reservar(new BigDecimal("3"));

        saldo.consumirReserva(new BigDecimal("3"));

        assertThat(saldo.getEstoqueFisico()).isEqualByComparingTo("7");
        assertThat(saldo.getQuantidadeReservada()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saldo.getDisponivel()).isEqualByComparingTo("7");
    }

    @Test
    void consumoDeReservaRejeitaQuandoSuperaReservado() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);
        saldo.registrarEntrada(new BigDecimal("10"));
        saldo.reservar(new BigDecimal("3"));

        assertThrows(RegraDeNegocioException.class, () -> saldo.consumirReserva(new BigDecimal("4")));
    }

    @Test
    void rejeitaQuantidadeZeroOuNegativaEmQualquerOperacao() {
        SaldoEstoque saldo = SaldoEstoque.novo(produtoId);
        saldo.registrarEntrada(new BigDecimal("10"));

        assertThrows(RegraDeNegocioException.class, () -> saldo.registrarEntrada(BigDecimal.ZERO));
        assertThrows(RegraDeNegocioException.class, () -> saldo.registrarSaida(new BigDecimal("-1")));
        assertThrows(RegraDeNegocioException.class, () -> saldo.reservar(BigDecimal.ZERO));
    }
}
