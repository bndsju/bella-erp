package com.bella.backend.adapter.in.web.fornecedor;

import jakarta.validation.constraints.NotBlank;

public record FornecedorRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        String telefone
) {
}
