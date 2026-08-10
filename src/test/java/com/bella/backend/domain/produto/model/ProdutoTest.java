package com.bella.backend.domain.produto.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProdutoTest {

    private static final UUID CATEGORIA_ID = UUID.randomUUID();
    private static final UUID UNIDADE_MEDIDA_ID = UUID.randomUUID();

    @Test
    void cadastraProdutoValido() {
        Produto produto = Produto.novo("SKU-001", "7891234567890", "Arroz Branco", "Arroz tipo 1",
                CATEGORIA_ID, UNIDADE_MEDIDA_ID, new BigDecimal("10.00"), new BigDecimal("15.00"),
                new BigDecimal("5"));

        assertThat(produto.getCodigoInterno()).isEqualTo("SKU-001");
        assertThat(produto.getNome()).isEqualTo("Arroz Branco");
        assertThat(produto.getStatus()).isEqualTo(StatusProduto.ATIVO);
    }

    @Test
    void rejeitaCodigoInternoVazio() {
        assertThrows(RegraDeNegocioException.class, () -> Produto.novo("", null, "Arroz Branco", null,
                CATEGORIA_ID, UNIDADE_MEDIDA_ID, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO));
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(RegraDeNegocioException.class, () -> Produto.novo("SKU-001", null, "", null,
                CATEGORIA_ID, UNIDADE_MEDIDA_ID, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO));
    }

    @Test
    void rejeitaCategoriaNula() {
        assertThrows(RegraDeNegocioException.class, () -> Produto.novo("SKU-001", null, "Arroz Branco", null,
                null, UNIDADE_MEDIDA_ID, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO));
    }

    @Test
    void rejeitaUnidadeMedidaNula() {
        assertThrows(RegraDeNegocioException.class, () -> Produto.novo("SKU-001", null, "Arroz Branco", null,
                CATEGORIA_ID, null, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO));
    }

    @Test
    void rejeitaPrecoCustoNegativo() {
        assertThrows(RegraDeNegocioException.class, () -> Produto.novo("SKU-001", null, "Arroz Branco", null,
                CATEGORIA_ID, UNIDADE_MEDIDA_ID, new BigDecimal("-1"), BigDecimal.TEN, BigDecimal.ZERO));
    }

    @Test
    void rejeitaPrecoVendaNegativo() {
        assertThrows(RegraDeNegocioException.class, () -> Produto.novo("SKU-001", null, "Arroz Branco", null,
                CATEGORIA_ID, UNIDADE_MEDIDA_ID, BigDecimal.TEN, new BigDecimal("-1"), BigDecimal.ZERO));
    }

    @Test
    void rejeitaEstoqueMinimoNegativo() {
        assertThrows(RegraDeNegocioException.class, () -> Produto.novo("SKU-001", null, "Arroz Branco", null,
                CATEGORIA_ID, UNIDADE_MEDIDA_ID, BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("-1")));
    }

    @Test
    void editarAtualizaDadosMasNaoOCodigoInterno() {
        Produto produto = Produto.novo("SKU-001", null, "Arroz Branco", null,
                CATEGORIA_ID, UNIDADE_MEDIDA_ID, new BigDecimal("10.00"), new BigDecimal("15.00"),
                new BigDecimal("5"));

        UUID novaCategoria = UUID.randomUUID();
        produto.editar("7891234567890", "Arroz Integral", "Arroz integral tipo 1", novaCategoria,
                UNIDADE_MEDIDA_ID, new BigDecimal("12.00"), new BigDecimal("18.00"), new BigDecimal("3"));

        assertThat(produto.getCodigoInterno()).isEqualTo("SKU-001");
        assertThat(produto.getNome()).isEqualTo("Arroz Integral");
        assertThat(produto.getCategoriaId()).isEqualTo(novaCategoria);
        assertThat(produto.getCodigoBarras()).isEqualTo("7891234567890");
    }

    @Test
    void ativarEInativarAlteramStatus() {
        Produto produto = Produto.novo("SKU-001", null, "Arroz Branco", null,
                CATEGORIA_ID, UNIDADE_MEDIDA_ID, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO);

        produto.inativar();
        assertThat(produto.getStatus()).isEqualTo(StatusProduto.INATIVO);

        produto.ativar();
        assertThat(produto.getStatus()).isEqualTo(StatusProduto.ATIVO);
    }
}
