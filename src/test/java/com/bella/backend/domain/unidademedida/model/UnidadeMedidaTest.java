package com.bella.backend.domain.unidademedida.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnidadeMedidaTest {

    @Test
    void cadastraUnidadeMedidaValida() {
        UnidadeMedida unidadeMedida = UnidadeMedida.novo("Quilograma", "kg");

        assertThat(unidadeMedida.getNome()).isEqualTo("Quilograma");
        assertThat(unidadeMedida.getSigla()).isEqualTo("kg");
        assertThat(unidadeMedida.getStatus()).isEqualTo(StatusUnidadeMedida.ATIVO);
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(RegraDeNegocioException.class, () -> UnidadeMedida.novo("", "kg"));
    }

    @Test
    void rejeitaSiglaVazia() {
        assertThrows(RegraDeNegocioException.class, () -> UnidadeMedida.novo("Quilograma", ""));
    }

    @Test
    void editarAtualizaNomeESigla() {
        UnidadeMedida unidadeMedida = UnidadeMedida.novo("Quilograma", "kg");
        unidadeMedida.editar("Grama", "g");

        assertThat(unidadeMedida.getNome()).isEqualTo("Grama");
        assertThat(unidadeMedida.getSigla()).isEqualTo("g");
    }

    @Test
    void ativarEInativarAlteramStatus() {
        UnidadeMedida unidadeMedida = UnidadeMedida.novo("Quilograma", "kg");

        unidadeMedida.inativar();
        assertThat(unidadeMedida.getStatus()).isEqualTo(StatusUnidadeMedida.INATIVO);

        unidadeMedida.ativar();
        assertThat(unidadeMedida.getStatus()).isEqualTo(StatusUnidadeMedida.ATIVO);
    }
}
