package com.bella.backend.domain.fornecedor.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FornecedorTest {

    @Test
    void cadastraFornecedorValida() {
        Fornecedor fornecedor = Fornecedor.novo("Rápido Entregas", "11988887777");

        assertThat(fornecedor.getNome()).isEqualTo("Rápido Entregas");
        assertThat(fornecedor.getStatus()).isEqualTo(StatusFornecedor.ATIVO);
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(RegraDeNegocioException.class, () -> Fornecedor.novo("", "11988887777"));
    }

    @Test
    void editarAtualizaNomeETelefone() {
        Fornecedor fornecedor = Fornecedor.novo("Rápido Entregas", "11988887777");
        fornecedor.editar("Rápido Entregas Ltda", "1133334444");

        assertThat(fornecedor.getNome()).isEqualTo("Rápido Entregas Ltda");
        assertThat(fornecedor.getTelefone()).isEqualTo("1133334444");
    }

    @Test
    void ativarEInativarAlteramStatus() {
        Fornecedor fornecedor = Fornecedor.novo("Rápido Entregas", null);

        fornecedor.inativar();
        assertThat(fornecedor.getStatus()).isEqualTo(StatusFornecedor.INATIVO);

        fornecedor.ativar();
        assertThat(fornecedor.getStatus()).isEqualTo(StatusFornecedor.ATIVO);
    }
}
