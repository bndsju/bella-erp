package com.bella.backend.domain.venda.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VendaTest {

    private static List<VendaItem> umItem() {
        return List.of(VendaItem.novo(UUID.randomUUID(), new BigDecimal("2"), new BigDecimal("50.00")));
    }

    private static Venda concluida() {
        return Venda.concluir(UUID.randomUUID(), UUID.randomUUID(), umItem(), new BigDecimal("10"),
                new BigDecimal("15.00"), FormaPagamento.PIX, "À vista");
    }

    @Test
    void concluiVendaValidaComoConcluida() {
        Venda venda = concluida();

        assertThat(venda.getStatus()).isEqualTo(StatusVenda.CONCLUIDA);
        assertThat(venda.getFormaPagamento()).isEqualTo(FormaPagamento.PIX);
    }

    @Test
    void calculaSubtotalDescontoETotalCorretamente() {
        Venda venda = concluida();

        assertThat(venda.getSubtotal()).isEqualByComparingTo("100.00");
        assertThat(venda.getValorDesconto()).isEqualByComparingTo("10.00");
        assertThat(venda.getValorTotal()).isEqualByComparingTo("105.00");
    }

    @Test
    void rejeitaPedidoOrigemNulo() {
        assertThrows(RegraDeNegocioException.class, () -> Venda.concluir(
                null, UUID.randomUUID(), umItem(), BigDecimal.ZERO, BigDecimal.ZERO,
                FormaPagamento.DINHEIRO, "À vista"));
    }

    @Test
    void rejeitaSemItens() {
        assertThrows(RegraDeNegocioException.class, () -> Venda.concluir(
                UUID.randomUUID(), UUID.randomUUID(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                FormaPagamento.DINHEIRO, "À vista"));
    }

    @Test
    void rejeitaFormaPagamentoNula() {
        assertThrows(RegraDeNegocioException.class, () -> Venda.concluir(
                UUID.randomUUID(), UUID.randomUUID(), umItem(), BigDecimal.ZERO, BigDecimal.ZERO,
                null, "À vista"));
    }

    @Test
    void rejeitaCondicaoPagamentoVazia() {
        assertThrows(RegraDeNegocioException.class, () -> Venda.concluir(
                UUID.randomUUID(), UUID.randomUUID(), umItem(), BigDecimal.ZERO, BigDecimal.ZERO,
                FormaPagamento.DINHEIRO, ""));
    }

    @Test
    void naoTemMetodoDeEdicao() {
        // Garantia estrutural: Venda expõe apenas cancelar() como transição pós-conclusão.
        // Se este teste falhar após adicionar um método editar(...), reveja se a regra de
        // negócio "impedir alterações comerciais indevidas após a conclusão" ainda se aplica.
        long metodosPublicosDeInstancia = java.util.Arrays.stream(Venda.class.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                .filter(m -> !java.lang.reflect.Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getName().startsWith("editar"))
                .count();

        assertThat(metodosPublicosDeInstancia).isZero();
    }

    @Test
    void cancelarMudaStatusParaCancelada() {
        Venda venda = concluida();
        venda.cancelar();

        assertThat(venda.getStatus()).isEqualTo(StatusVenda.CANCELADA);
    }

    @Test
    void cancelarRejeitaQuandoJaCancelada() {
        Instant agora = Instant.now();
        Venda venda = Venda.existente(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), umItem(),
                BigDecimal.ZERO, BigDecimal.ZERO, FormaPagamento.DINHEIRO, "À vista", StatusVenda.CANCELADA,
                agora, agora);

        assertThrows(RegraDeNegocioException.class, venda::cancelar);
    }
}
