package com.bella.backend.domain.transportadora.port.in;

import com.bella.backend.domain.transportadora.model.Transportadora;

public interface CadastrarTransportadoraUseCase {

    Transportadora cadastrar(Comando comando);

    record Comando(String nome, String telefone) {
    }
}
