package com.bella.backend.domain.cliente.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientePessoaJuridicaTest {

    private static final Endereco ENDERECO =
            new Endereco("01310-100", "Av. Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");

    @Test
    void cadastraClientePessoaJuridicaValido() {
        ClientePessoaJuridica cliente = ClientePessoaJuridica.novo(
                "Padaria Pão Quente Ltda", "Pão Quente", "11.222.333/0001-81",
                "123456789", "987654321", "11999998888", "contato@paoquente.com", null, ENDERECO, null);

        assertThat(cliente.getRazaoSocial()).isEqualTo("Padaria Pão Quente Ltda");
        assertThat(cliente.getCnpj()).isEqualTo("11222333000181");
        assertThat(cliente.getTipoPessoa()).isEqualTo(TipoPessoa.PJ);
    }

    @Test
    void rejeitaRazaoSocialVazia() {
        assertThrows(RegraDeNegocioException.class, () -> ClientePessoaJuridica.novo(
                "", "Pão Quente", "11.222.333/0001-81",
                null, null, "11999998888", "contato@paoquente.com", null, ENDERECO, null));
    }

    @Test
    void rejeitaCnpjInvalido() {
        assertThrows(RegraDeNegocioException.class, () -> ClientePessoaJuridica.novo(
                "Padaria Pão Quente Ltda", "Pão Quente", "11222333000199",
                null, null, "11999998888", "contato@paoquente.com", null, ENDERECO, null));
    }

    @Test
    void editarAtualizaDadosMasNaoOCnpj() {
        ClientePessoaJuridica cliente = ClientePessoaJuridica.novo(
                "Padaria Pão Quente Ltda", "Pão Quente", "11.222.333/0001-81",
                null, null, "11999998888", "contato@paoquente.com", null, ENDERECO, null);

        cliente.editar("Padaria Pão Quentinho Ltda", "Pão Quentinho", "111", "222",
                "11888887777", "novo@paoquente.com", null, ENDERECO, "observação");

        assertThat(cliente.getRazaoSocial()).isEqualTo("Padaria Pão Quentinho Ltda");
        assertThat(cliente.getCnpj()).isEqualTo("11222333000181");
    }
}
