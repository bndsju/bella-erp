package com.bella.backend.domain.unidademedida.port.in;

import com.bella.backend.domain.unidademedida.model.UnidadeMedida;

public interface CadastrarUnidadeMedidaUseCase {

    UnidadeMedida cadastrar(Comando comando);

    record Comando(String nome, String sigla) {
    }
}
