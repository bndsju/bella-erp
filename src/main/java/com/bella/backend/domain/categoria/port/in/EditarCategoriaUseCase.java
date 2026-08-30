package com.bella.backend.domain.categoria.port.in;

import com.bella.backend.domain.categoria.model.Categoria;

import java.util.UUID;

public interface EditarCategoriaUseCase {

    Categoria editar(UUID id, Comando comando);

    record Comando(String nome) {
    }
}
