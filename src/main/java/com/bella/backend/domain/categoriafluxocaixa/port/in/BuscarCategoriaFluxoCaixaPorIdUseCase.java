package com.bella.backend.domain.categoriafluxocaixa.port.in;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;

import java.util.UUID;

public interface BuscarCategoriaFluxoCaixaPorIdUseCase {

    CategoriaFluxoCaixa buscarPorId(UUID id);
}
