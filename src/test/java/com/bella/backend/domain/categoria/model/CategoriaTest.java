package com.bella.backend.domain.categoria.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoriaTest {

    @Test
    void cadastraCategoriaValida() {
        Categoria categoria = Categoria.novo("Bebidas");

        assertThat(categoria.getNome()).isEqualTo("Bebidas");
        assertThat(categoria.getStatus()).isEqualTo(StatusCategoria.ATIVO);
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(RegraDeNegocioException.class, () -> Categoria.novo(""));
    }

    @Test
    void editarAtualizaNome() {
        Categoria categoria = Categoria.novo("Bebidas");
        categoria.editar("Bebidas Geladas");
        assertThat(categoria.getNome()).isEqualTo("Bebidas Geladas");
    }

    @Test
    void ativarEInativarAlteramStatus() {
        Categoria categoria = Categoria.novo("Bebidas");

        categoria.inativar();
        assertThat(categoria.getStatus()).isEqualTo(StatusCategoria.INATIVO);

        categoria.ativar();
        assertThat(categoria.getStatus()).isEqualTo(StatusCategoria.ATIVO);
    }
}
