package com.bella.backend.domain.transportadora.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransportadoraTest {

    @Test
    void cadastraTransportadoraValida() {
        Transportadora transportadora = Transportadora.novo("Rápido Entregas", "11988887777");

        assertThat(transportadora.getNome()).isEqualTo("Rápido Entregas");
        assertThat(transportadora.getStatus()).isEqualTo(StatusTransportadora.ATIVO);
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(RegraDeNegocioException.class, () -> Transportadora.novo("", "11988887777"));
    }

    @Test
    void editarAtualizaNomeETelefone() {
        Transportadora transportadora = Transportadora.novo("Rápido Entregas", "11988887777");
        transportadora.editar("Rápido Entregas Ltda", "1133334444");

        assertThat(transportadora.getNome()).isEqualTo("Rápido Entregas Ltda");
        assertThat(transportadora.getTelefone()).isEqualTo("1133334444");
    }

    @Test
    void ativarEInativarAlteramStatus() {
        Transportadora transportadora = Transportadora.novo("Rápido Entregas", null);

        transportadora.inativar();
        assertThat(transportadora.getStatus()).isEqualTo(StatusTransportadora.INATIVO);

        transportadora.ativar();
        assertThat(transportadora.getStatus()).isEqualTo(StatusTransportadora.ATIVO);
    }
}
