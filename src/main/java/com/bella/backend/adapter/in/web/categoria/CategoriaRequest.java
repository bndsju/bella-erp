package com.bella.backend.adapter.in.web.categoria;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank(message = "Nome é obrigatório") String nome
) {
}
