package com.bella.backend.domain.categoriafluxocaixa.port.in;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;

public interface CadastrarCategoriaFluxoCaixaUseCase {

    CategoriaFluxoCaixa cadastrar(Comando comando);

    record Comando(String nome) {
    }
}
