package com.bella.backend.domain.cliente.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientePessoaFisicaTest {

    private static final Endereco ENDERECO =
            new Endereco("01310-100", "Av. Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");

    @Test
    void cadastraClientePessoaFisicaValido() {
        ClientePessoaFisica cliente = ClientePessoaFisica.novo(
                "Maria Silva", LocalDate.of(1990, 5, 10), "111.444.777-35",
                "11999998888", "maria@example.com", null, ENDERECO, null);

        assertThat(cliente.getNomeCompleto()).isEqualTo("Maria Silva");
        assertThat(cliente.getCpf()).isEqualTo("11144477735");
        assertThat(cliente.getTipoPessoa()).isEqualTo(TipoPessoa.PF);
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.ATIVO);
    }

    @Test
    void rejeitaDataDeNascimentoFutura() {
        assertThrows(RegraDeNegocioException.class, () -> ClientePessoaFisica.novo(
                "Maria Silva", LocalDate.now().plusDays(1), "111.444.777-35",
                "11999998888", "maria@example.com", null, ENDERECO, null));
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(RegraDeNegocioException.class, () -> ClientePessoaFisica.novo(
                "", LocalDate.of(1990, 5, 10), "111.444.777-35",
                "11999998888", "maria@example.com", null, ENDERECO, null));
    }

    @Test
    void rejeitaCpfInvalido() {
        assertThrows(RegraDeNegocioException.class, () -> ClientePessoaFisica.novo(
                "Maria Silva", LocalDate.of(1990, 5, 10), "12345678900",
                "11999998888", "maria@example.com", null, ENDERECO, null));
    }

    @Test
    void editarAtualizaDadosMasNaoOCpf() {
        ClientePessoaFisica cliente = ClientePessoaFisica.novo(
                "Maria Silva", LocalDate.of(1990, 5, 10), "111.444.777-35",
                "11999998888", "maria@example.com", null, ENDERECO, null);

        cliente.editar("Maria S. Silva", LocalDate.of(1991, 1, 1), "11888887777",
                "maria2@example.com", "1133334444", ENDERECO, "cliente VIP");

        assertThat(cliente.getNomeCompleto()).isEqualTo("Maria S. Silva");
        assertThat(cliente.getCelular()).isEqualTo("11888887777");
        assertThat(cliente.getCpf()).isEqualTo("11144477735");
    }

    @Test
    void ativarEInativarAlteramStatus() {
        ClientePessoaFisica cliente = ClientePessoaFisica.novo(
                "Maria Silva", LocalDate.of(1990, 5, 10), "111.444.777-35",
                "11999998888", "maria@example.com", null, ENDERECO, null);

        cliente.inativar();
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.INATIVO);

        cliente.ativar();
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.ATIVO);
    }
}
