package com.bella.backend.domain.categoriafluxocaixa.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoriaFluxoCaixaTest {

    @Test
    void cadastraCategoriaFluxoCaixaValida() {
        CategoriaFluxoCaixa categoriaFluxoCaixa = CategoriaFluxoCaixa.novo("Bebidas");

        assertThat(categoriaFluxoCaixa.getNome()).isEqualTo("Bebidas");
        assertThat(categoriaFluxoCaixa.getStatus()).isEqualTo(StatusCategoriaFluxoCaixa.ATIVO);
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(RegraDeNegocioException.class, () -> CategoriaFluxoCaixa.novo(""));
    }

    @Test
    void editarAtualizaNome() {
        CategoriaFluxoCaixa categoriaFluxoCaixa = CategoriaFluxoCaixa.novo("Bebidas");
        categoriaFluxoCaixa.editar("Bebidas Geladas");
        assertThat(categoriaFluxoCaixa.getNome()).isEqualTo("Bebidas Geladas");
    }

    @Test
    void ativarEInativarAlteramStatus() {
        CategoriaFluxoCaixa categoriaFluxoCaixa = CategoriaFluxoCaixa.novo("Bebidas");

        categoriaFluxoCaixa.inativar();
        assertThat(categoriaFluxoCaixa.getStatus()).isEqualTo(StatusCategoriaFluxoCaixa.INATIVO);

        categoriaFluxoCaixa.ativar();
        assertThat(categoriaFluxoCaixa.getStatus()).isEqualTo(StatusCategoriaFluxoCaixa.ATIVO);
    }
}
