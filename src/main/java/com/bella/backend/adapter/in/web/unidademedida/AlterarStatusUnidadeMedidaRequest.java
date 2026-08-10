package com.bella.backend.adapter.in.web.unidademedida;

import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusUnidadeMedidaRequest(
        @NotNull(message = "Status é obrigatório") StatusUnidadeMedida status
) {
}
