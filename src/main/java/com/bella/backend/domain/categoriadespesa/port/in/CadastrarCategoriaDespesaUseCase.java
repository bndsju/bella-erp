package com.bella.backend.domain.categoriadespesa.port.in;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;

public interface CadastrarCategoriaDespesaUseCase {

    CategoriaDespesa cadastrar(Comando comando);

    record Comando(String nome) {
    }
}
