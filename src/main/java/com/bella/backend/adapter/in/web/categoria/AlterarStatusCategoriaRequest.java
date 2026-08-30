package com.bella.backend.adapter.in.web.categoria;

import com.bella.backend.domain.categoria.model.StatusCategoria;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusCategoriaRequest(
        @NotNull(message = "Status é obrigatório") StatusCategoria status
) {
}
