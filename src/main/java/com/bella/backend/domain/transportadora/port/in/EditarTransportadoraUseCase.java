package com.bella.backend.domain.transportadora.port.in;

import com.bella.backend.domain.transportadora.model.Transportadora;

import java.util.UUID;

public interface EditarTransportadoraUseCase {

    Transportadora editar(UUID id, Comando comando);

    record Comando(String nome, String telefone) {
    }
}
