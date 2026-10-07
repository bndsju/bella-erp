package com.bella.backend.adapter.in.web.categoriadespesa;

import jakarta.validation.constraints.NotBlank;

public record CategoriaDespesaRequest(
        @NotBlank(message = "Nome é obrigatório") String nome
) {
}
