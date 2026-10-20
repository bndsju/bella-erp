package com.bella.backend.adapter.in.web.categoriafluxocaixa;

import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusCategoriaFluxoCaixaRequest(
        @NotNull(message = "Status é obrigatório") StatusCategoriaFluxoCaixa status
) {
}
