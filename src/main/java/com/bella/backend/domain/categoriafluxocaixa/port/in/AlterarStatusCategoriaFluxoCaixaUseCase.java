package com.bella.backend.domain.categoriafluxocaixa.port.in;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;

import java.util.UUID;

public interface AlterarStatusCategoriaFluxoCaixaUseCase {

    CategoriaFluxoCaixa alterarStatus(UUID id, StatusCategoriaFluxoCaixa novoStatus);
}
