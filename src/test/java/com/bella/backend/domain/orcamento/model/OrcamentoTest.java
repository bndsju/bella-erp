package com.bella.backend.domain.orcamento.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrcamentoTest {

    private static List<OrcamentoItem> umItem() {
        return List.of(OrcamentoItem.novo(UUID.randomUUID(), new BigDecimal("2"), new BigDecimal("50.00")));
    }

    private static Orcamento novoRascunho() {
        return Orcamento.novo(UUID.randomUUID(), umItem(), new BigDecimal("10"), new BigDecimal("15.00"),
                LocalDate.now().plusDays(7), "À vista", null);
    }

    @Test
    void cadastraOrcamentoValidoEmRascunho() {
        Orcamento orcamento = novoRascunho();

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.RASCUNHO);
        assertThat(orcamento.getItens()).hasSize(1);
    }

    @Test
    void calculaSubtotalDescontoETotalCorretamente() {
        // item: 2 x 50.00 = 100.00 subtotal; 10% desconto = 10.00; frete 15.00
        Orcamento orcamento = novoRascunho();

        assertThat(orcamento.getSubtotal()).isEqualByComparingTo("100.00");
        assertThat(orcamento.getValorDesconto()).isEqualByComparingTo("10.00");
        assertThat(orcamento.getValorTotal()).isEqualByComparingTo("105.00");
    }

    @Test
    void rejeitaClienteNulo() {
        assertThrows(RegraDeNegocioException.class, () -> Orcamento.novo(
                null, umItem(), BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now().plusDays(1), "À vista", null));
    }

    @Test
    void rejeitaPercentualDescontoForaDoIntervalo() {
        assertThrows(RegraDeNegocioException.class, () -> Orcamento.novo(
                UUID.randomUUID(), umItem(), new BigDecimal("101"), BigDecimal.ZERO,
                LocalDate.now().plusDays(1), "À vista", null));
        assertThrows(RegraDeNegocioException.class, () -> Orcamento.novo(
                UUID.randomUUID(), umItem(), new BigDecimal("-1"), BigDecimal.ZERO,
                LocalDate.now().plusDays(1), "À vista", null));
    }

    @Test
    void rejeitaValorFreteNegativo() {
        assertThrows(RegraDeNegocioException.class, () -> Orcamento.novo(
                UUID.randomUUID(), umItem(), BigDecimal.ZERO, new BigDecimal("-1"),
                LocalDate.now().plusDays(1), "À vista", null));
    }

    @Test
    void rejeitaPrazoValidadeNoPassado() {
        assertThrows(RegraDeNegocioException.class, () -> Orcamento.novo(
                UUID.randomUUID(), umItem(), BigDecimal.ZERO, BigDecimal.ZERO,
                LocalDate.now().minusDays(1), "À vista", null));
    }

    @Test
    void rejeitaCondicaoPagamentoVazia() {
        assertThrows(RegraDeNegocioException.class, () -> Orcamento.novo(
                UUID.randomUUID(), umItem(), BigDecimal.ZERO, BigDecimal.ZERO,
                LocalDate.now().plusDays(1), "", null));
    }

    @Test
    void editarSoFuncionaEmRascunho() {
        Orcamento orcamento = novoRascunho();
        orcamento.enviar();

        assertThrows(RegraDeNegocioException.class, () -> orcamento.editar(
                UUID.randomUUID(), umItem(), BigDecimal.ZERO, BigDecimal.ZERO,
                LocalDate.now().plusDays(1), "À vista", null));
    }

    @Test
    void enviarMudaParaEnviadoQuandoRascunhoComItens() {
        Orcamento orcamento = novoRascunho();
        orcamento.enviar();
        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.ENVIADO);
    }

    @Test
    void enviarRejeitaOrcamentoSemItens() {
        Orcamento orcamento = Orcamento.novo(UUID.randomUUID(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                LocalDate.now().plusDays(1), "À vista", null);

        assertThrows(RegraDeNegocioException.class, orcamento::enviar);
    }

    @Test
    void enviarRejeitaQuandoJaNaoEstaEmRascunho() {
        Orcamento orcamento = novoRascunho();
        orcamento.enviar();

        assertThrows(RegraDeNegocioException.class, orcamento::enviar);
    }

    @Test
    void aprovarSoFuncionaQuandoEnviado() {
        Orcamento orcamento = novoRascunho();
        assertThrows(RegraDeNegocioException.class, orcamento::aprovar);

        orcamento.enviar();
        orcamento.aprovar();
        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.APROVADO);
    }

    @Test
    void recusarSoFuncionaQuandoEnviado() {
        Orcamento orcamento = novoRascunho();
        assertThrows(RegraDeNegocioException.class, orcamento::recusar);

        orcamento.enviar();
        orcamento.recusar();
        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.RECUSADO);
    }

    @Test
    void cancelarFuncionaEmRascunhoEEnviado() {
        Orcamento rascunho = novoRascunho();
        rascunho.cancelar();
        assertThat(rascunho.getStatus()).isEqualTo(StatusOrcamento.CANCELADO);

        Orcamento enviado = novoRascunho();
        enviado.enviar();
        enviado.cancelar();
        assertThat(enviado.getStatus()).isEqualTo(StatusOrcamento.CANCELADO);
    }

    @Test
    void cancelarRejeitaEstadosFinais() {
        Orcamento aprovado = novoRascunho();
        aprovado.enviar();
        aprovado.aprovar();

        assertThrows(RegraDeNegocioException.class, aprovado::cancelar);
    }

    @Test
    void expirarSeNecessarioMudaParaExpiradoQuandoPrazoPassouEEstaEnviado() {
        Instant agora = Instant.now();
        Orcamento orcamento = Orcamento.existente(UUID.randomUUID(), UUID.randomUUID(), umItem(),
                BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now().minusDays(1), "À vista", null,
                StatusOrcamento.ENVIADO, agora, agora);

        boolean expirou = orcamento.expirarSeNecessario();

        assertThat(expirou).isTrue();
        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.EXPIRADO);
    }

    @Test
    void expirarSeNecessarioNaoAlteraRascunho() {
        Instant agora = Instant.now();
        Orcamento orcamento = Orcamento.existente(UUID.randomUUID(), UUID.randomUUID(), umItem(),
                BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now().minusDays(1), "À vista", null,
                StatusOrcamento.RASCUNHO, agora, agora);

        boolean expirou = orcamento.expirarSeNecessario();

        assertThat(expirou).isFalse();
        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.RASCUNHO);
    }
}
