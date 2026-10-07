package com.bella.backend.adapter.in.web.fornecedor;

import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusFornecedorRequest(
        @NotNull(message = "Status é obrigatório") StatusFornecedor status
) {
}
