package com.bella.backend.domain.categoriadespesa.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoriaDespesaTest {

    @Test
    void cadastraCategoriaDespesaValida() {
        CategoriaDespesa categoriaDespesa = CategoriaDespesa.novo("Bebidas");

        assertThat(categoriaDespesa.getNome()).isEqualTo("Bebidas");
        assertThat(categoriaDespesa.getStatus()).isEqualTo(StatusCategoriaDespesa.ATIVO);
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(RegraDeNegocioException.class, () -> CategoriaDespesa.novo(""));
    }

    @Test
    void editarAtualizaNome() {
        CategoriaDespesa categoriaDespesa = CategoriaDespesa.novo("Bebidas");
        categoriaDespesa.editar("Bebidas Geladas");
        assertThat(categoriaDespesa.getNome()).isEqualTo("Bebidas Geladas");
    }

    @Test
    void ativarEInativarAlteramStatus() {
        CategoriaDespesa categoriaDespesa = CategoriaDespesa.novo("Bebidas");

        categoriaDespesa.inativar();
        assertThat(categoriaDespesa.getStatus()).isEqualTo(StatusCategoriaDespesa.INATIVO);

        categoriaDespesa.ativar();
        assertThat(categoriaDespesa.getStatus()).isEqualTo(StatusCategoriaDespesa.ATIVO);
    }
}
