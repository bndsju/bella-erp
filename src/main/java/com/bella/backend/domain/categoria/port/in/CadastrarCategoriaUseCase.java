package com.bella.backend.domain.categoria.port.in;

import com.bella.backend.domain.categoria.model.Categoria;

public interface CadastrarCategoriaUseCase {

    Categoria cadastrar(Comando comando);

    record Comando(String nome) {
    }
}
