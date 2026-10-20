package com.bella.backend.domain.categoriadespesa.port.in;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;

import java.util.UUID;

public interface EditarCategoriaDespesaUseCase {

    CategoriaDespesa editar(UUID id, Comando comando);

    record Comando(String nome) {
    }
}
