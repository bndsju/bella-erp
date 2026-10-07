package com.bella.backend.adapter.in.web.categoriadespesa;

import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusCategoriaDespesaRequest(
        @NotNull(message = "Status é obrigatório") StatusCategoriaDespesa status
) {
}
