package com.bella.backend.adapter.in.web.produto;

import com.bella.backend.domain.produto.model.StatusProduto;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusProdutoRequest(
        @NotNull(message = "Status é obrigatório") StatusProduto status
) {
}
