package com.bella.backend.adapter.in.web.categoriafluxocaixa;

import jakarta.validation.constraints.NotBlank;

public record CategoriaFluxoCaixaRequest(
        @NotBlank(message = "Nome é obrigatório") String nome
) {
}
