package com.bella.backend.domain.compra.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompraTest {

    private static final UUID FORNECEDOR_ID = UUID.randomUUID();
    private static final UUID PRODUTO_A = UUID.randomUUID();
    private static final UUID PRODUTO_B = UUID.randomUUID();

    private CompraItem itemA() {
        return CompraItem.novo(PRODUTO_A, new BigDecimal("10"), new BigDecimal("5.00"), new BigDecimal("5.00"));
    }

    private CompraItem itemB() {
        return CompraItem.novo(PRODUTO_B, new BigDecimal("2"), new BigDecimal("20.00"), null);
    }

    private Compra compraValida() {
        return Compra.nova(FORNECEDOR_ID, LocalDate.now().plusDays(7), List.of(itemA(), itemB()),
                new BigDecimal("10.00"), new BigDecimal("15.00"), new BigDecimal("5.00"), "30 dias", null);
    }

    @Test
    void criaCompraValidaEmRascunho() {
        Compra compra = compraValida();

        assertThat(compra.getStatus()).isEqualTo(StatusCompra.RASCUNHO);
        assertThat(compra.getDataCompra()).isEqualTo(LocalDate.now());
        assertThat(compra.getItens()).hasSize(2);
    }

    @Test
    void calculaTotaisNoDominio() {
        Compra compra = compraValida();

        // itens: 10x5,00=50,00 (desc. 5,00) e 2x20,00=40,00 -> produtos 90,00
        assertThat(compra.getValorProdutos()).isEqualByComparingTo("90.00");
        // descontos: 5,00 dos itens + 10,00 geral
        assertThat(compra.getDescontoTotal()).isEqualByComparingTo("15.00");
        // 90,00 - 15,00 + 15,00 frete + 5,00 outras
        assertThat(compra.getValorTotal()).isEqualByComparingTo("95.00");
        assertThat(compra.getItens().get(0).getValorTotal()).isEqualByComparingTo("45.00");
    }

    @Test
    void rejeitaCompraSemFornecedor() {
        assertThrows(RegraDeNegocioException.class, () -> Compra.nova(null, null, List.of(itemA()),
                null, null, null, "À vista", null));
    }

    @Test
    void rejeitaCompraSemItens() {
        assertThrows(RegraDeNegocioException.class, () -> Compra.nova(FORNECEDOR_ID, null, List.of(),
                null, null, null, "À vista", null));
    }

    @Test
    void rejeitaItemComQuantidadeInvalida() {
        assertThrows(RegraDeNegocioException.class,
                () -> CompraItem.novo(PRODUTO_A, BigDecimal.ZERO, BigDecimal.TEN, null));
        assertThrows(RegraDeNegocioException.class,
                () -> CompraItem.novo(PRODUTO_A, new BigDecimal("-1"), BigDecimal.TEN, null));
    }

    @Test
    void rejeitaCustoNegativoEDescontoMaiorQueItem() {
        assertThrows(RegraDeNegocioException.class,
                () -> CompraItem.novo(PRODUTO_A, BigDecimal.ONE, new BigDecimal("-1"), null));
        assertThrows(RegraDeNegocioException.class,
                () -> CompraItem.novo(PRODUTO_A, BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("10.01")));
    }

    @Test
    void rejeitaFreteOutrasDespesasEDescontoNegativos() {
        assertThrows(RegraDeNegocioException.class, () -> Compra.nova(FORNECEDOR_ID, null, List.of(itemA()),
                null, new BigDecimal("-1"), null, "À vista", null));
        assertThrows(RegraDeNegocioException.class, () -> Compra.nova(FORNECEDOR_ID, null, List.of(itemA()),
                null, null, new BigDecimal("-1"), "À vista", null));
        assertThrows(RegraDeNegocioException.class, () -> Compra.nova(FORNECEDOR_ID, null, List.of(itemA()),
                new BigDecimal("-1"), null, null, "À vista", null));
    }

    @Test
    void rejeitaDescontoGeralQueZeraOuNegativaOsProdutos() {
        assertThrows(RegraDeNegocioException.class, () -> Compra.nova(FORNECEDOR_ID, null, List.of(itemB()),
                new BigDecimal("40.01"), null, null, "À vista", null));
    }

    @Test
    void rejeitaProdutoDuplicado() {
        assertThrows(RegraDeNegocioException.class, () -> Compra.nova(FORNECEDOR_ID, null,
                List.of(itemA(), itemA()), null, null, null, "À vista", null));
    }

    @Test
    void confirmaCompraEmRascunho() {
        Compra compra = compraValida();

        compra.confirmar();

        assertThat(compra.getStatus()).isEqualTo(StatusCompra.CONFIRMADA);
        assertThat(compra.getConfirmadaEm()).isNotNull();
    }

    @Test
    void editaApenasEmRascunho() {
        Compra compra = compraValida();
        compra.editar(FORNECEDOR_ID, null, List.of(itemB()), null, null, null, "À vista", "novo");
        assertThat(compra.getItens()).hasSize(1);

        compra.confirmar();
        assertThrows(RegraDeNegocioException.class,
                () -> compra.editar(FORNECEDOR_ID, null, List.of(itemB()), null, null, null, "À vista", null));
    }

    @Test
    void recebeCompraConfirmadaUmaUnicaVez() {
        Compra compra = compraValida();
        compra.confirmar();

        compra.receber(null);

        assertThat(compra.getStatus()).isEqualTo(StatusCompra.RECEBIDA);
        assertThat(compra.getDataRecebimento()).isEqualTo(LocalDate.now());
        assertThrows(RegraDeNegocioException.class, () -> compra.receber(null));
    }

    @Test
    void naoRecebeCompraEmRascunhoOuCancelada() {
        Compra rascunho = compraValida();
        assertThrows(RegraDeNegocioException.class, () -> rascunho.receber(null));

        Compra cancelada = compraValida();
        cancelada.cancelar("Desistência");
        assertThrows(RegraDeNegocioException.class, () -> cancelada.receber(null));
    }

    @Test
    void compraRecebidaNaoPodeSerEditadaNemCancelada() {
        Compra compra = compraValida();
        compra.confirmar();
        compra.receber(null);

        assertThrows(RegraDeNegocioException.class,
                () -> compra.editar(FORNECEDOR_ID, null, List.of(itemB()), null, null, null, "À vista", null));
        assertThrows(RegraDeNegocioException.class, () -> compra.cancelar("Tarde demais"));
        assertThrows(RegraDeNegocioException.class, compra::confirmar);
    }

    @Test
    void cancelamentoExigeMotivoERegistraStatus() {
        Compra compra = compraValida();

        assertThrows(RegraDeNegocioException.class, () -> compra.cancelar(" "));
        compra.cancelar("Fornecedor sem estoque");

        assertThat(compra.getStatus()).isEqualTo(StatusCompra.CANCELADA);
        assertThat(compra.getMotivoCancelamento()).isEqualTo("Fornecedor sem estoque");
        assertThat(compra.getCanceladaEm()).isNotNull();
    }

    @Test
    void compraCanceladaNaoVoltaAoFluxo() {
        Compra compra = compraValida();
        compra.cancelar("Desistência");

        assertThrows(RegraDeNegocioException.class, compra::confirmar);
        assertThrows(RegraDeNegocioException.class, () -> compra.cancelar("De novo"));
        assertThrows(RegraDeNegocioException.class,
                () -> compra.editar(FORNECEDOR_ID, null, List.of(itemB()), null, null, null, "À vista", null));
    }
}
