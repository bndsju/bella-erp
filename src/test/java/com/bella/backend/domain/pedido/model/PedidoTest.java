package com.bella.backend.domain.pedido.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoTest {

    private static List<PedidoItem> umItem() {
        return List.of(PedidoItem.novo(UUID.randomUUID(), new BigDecimal("2"), new BigDecimal("50.00")));
    }

    private static Pedido novoPedido() {
        return Pedido.novo(UUID.randomUUID(), null, null, umItem(), new BigDecimal("10"), new BigDecimal("15.00"),
                "À vista");
    }

    @Test
    void cadastraPedidoValidoComoCriado() {
        Pedido pedido = novoPedido();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedido.getOrcamentoOrigemId()).isNull();
    }

    @Test
    void calculaSubtotalDescontoETotalCorretamente() {
        Pedido pedido = novoPedido();

        assertThat(pedido.getSubtotal()).isEqualByComparingTo("100.00");
        assertThat(pedido.getValorDesconto()).isEqualByComparingTo("10.00");
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("105.00");
    }

    @Test
    void rejeitaPedidoSemItens() {
        assertThrows(RegraDeNegocioException.class, () -> Pedido.novo(
                UUID.randomUUID(), null, null, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, "À vista"));
    }

    @Test
    void rejeitaClienteNulo() {
        assertThrows(RegraDeNegocioException.class, () -> Pedido.novo(
                null, null, null, umItem(), BigDecimal.ZERO, BigDecimal.ZERO, "À vista"));
    }

    @Test
    void rejeitaPercentualDescontoForaDoIntervalo() {
        assertThrows(RegraDeNegocioException.class, () -> Pedido.novo(
                UUID.randomUUID(), null, null, umItem(), new BigDecimal("101"), BigDecimal.ZERO, "À vista"));
    }

    @Test
    void rejeitaCondicaoPagamentoVazia() {
        assertThrows(RegraDeNegocioException.class, () -> Pedido.novo(
                UUID.randomUUID(), null, null, umItem(), BigDecimal.ZERO, BigDecimal.ZERO, ""));
    }

    @Test
    void fluxoCompletoDeStatusFuncionaNaOrdemCorreta() {
        Pedido pedido = novoPedido();

        pedido.confirmar();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);

        pedido.iniciarSeparacao();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_SEPARACAO);

        pedido.marcarProntoParaEntrega();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PRONTO_PARA_ENTREGA);

        pedido.entregar();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
    }

    @Test
    void naoPermitePularEtapas() {
        Pedido pedido = novoPedido();

        assertThrows(RegraDeNegocioException.class, pedido::iniciarSeparacao);
        assertThrows(RegraDeNegocioException.class, pedido::marcarProntoParaEntrega);
        assertThrows(RegraDeNegocioException.class, pedido::entregar);
    }

    @Test
    void cancelarFuncionaAntesDeEntregue() {
        Pedido criado = novoPedido();
        criado.cancelar();
        assertThat(criado.getStatus()).isEqualTo(StatusPedido.CANCELADO);

        Pedido emSeparacao = novoPedido();
        emSeparacao.confirmar();
        emSeparacao.iniciarSeparacao();
        emSeparacao.cancelar();
        assertThat(emSeparacao.getStatus()).isEqualTo(StatusPedido.CANCELADO);
    }

    @Test
    void cancelarRejeitaQuandoJaEntregueOuCancelado() {
        Pedido entregue = novoPedido();
        entregue.confirmar();
        entregue.iniciarSeparacao();
        entregue.marcarProntoParaEntrega();
        entregue.entregar();

        assertThrows(RegraDeNegocioException.class, entregue::cancelar);

        Pedido cancelado = novoPedido();
        cancelado.cancelar();
        assertThrows(RegraDeNegocioException.class, cancelado::cancelar);
    }

    @Test
    void editarSoFuncionaAntesDeEntregueOuCancelado() {
        Pedido pedido = novoPedido();
        pedido.confirmar();

        pedido.editar(UUID.randomUUID(), null, umItem(), BigDecimal.ZERO, BigDecimal.ZERO, "30 dias");
        assertThat(pedido.getCondicaoPagamento()).isEqualTo("30 dias");

        pedido.iniciarSeparacao();
        pedido.marcarProntoParaEntrega();
        pedido.entregar();

        assertThrows(RegraDeNegocioException.class, () -> pedido.editar(
                UUID.randomUUID(), null, umItem(), BigDecimal.ZERO, BigDecimal.ZERO, "À vista"));
    }

    @Test
    void orcamentoOrigemPermaneceAposEdicao() {
        UUID orcamentoId = UUID.randomUUID();
        Instant agora = Instant.now();
        Pedido pedido = Pedido.existente(UUID.randomUUID(), UUID.randomUUID(), orcamentoId, null, umItem(),
                BigDecimal.ZERO, BigDecimal.ZERO, "À vista", StatusPedido.CRIADO, agora, agora);

        pedido.editar(UUID.randomUUID(), null, umItem(), BigDecimal.ZERO, BigDecimal.ZERO, "30 dias");

        assertThat(pedido.getOrcamentoOrigemId()).isEqualTo(orcamentoId);
    }
}
