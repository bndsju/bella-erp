package com.bella.backend.domain.pedido.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoItemTest {

    @Test
    void calculaSubtotalComoQuantidadeVezesValorUnitario() {
        PedidoItem item = PedidoItem.novo(UUID.randomUUID(), new BigDecimal("3"), new BigDecimal("10.00"));

        assertThat(item.getSubtotal()).isEqualByComparingTo("30.00");
    }

    @Test
    void rejeitaProdutoNulo() {
        assertThrows(RegraDeNegocioException.class,
                () -> PedidoItem.novo(null, BigDecimal.ONE, BigDecimal.TEN));
    }

    @Test
    void rejeitaQuantidadeZeroOuNegativa() {
        assertThrows(RegraDeNegocioException.class,
                () -> PedidoItem.novo(UUID.randomUUID(), BigDecimal.ZERO, BigDecimal.TEN));
        assertThrows(RegraDeNegocioException.class,
                () -> PedidoItem.novo(UUID.randomUUID(), new BigDecimal("-1"), BigDecimal.TEN));
    }

    @Test
    void rejeitaValorUnitarioNegativo() {
        assertThrows(RegraDeNegocioException.class,
                () -> PedidoItem.novo(UUID.randomUUID(), BigDecimal.ONE, new BigDecimal("-0.01")));
    }
}
