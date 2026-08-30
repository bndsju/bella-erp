package com.bella.backend.domain.unidademedida.port.in;

import com.bella.backend.domain.unidademedida.model.UnidadeMedida;

import java.util.UUID;

public interface EditarUnidadeMedidaUseCase {

    UnidadeMedida editar(UUID id, Comando comando);

    record Comando(String nome, String sigla) {
    }
}
