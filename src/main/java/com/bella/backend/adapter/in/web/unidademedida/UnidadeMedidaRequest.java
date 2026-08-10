package com.bella.backend.adapter.in.web.unidademedida;

import jakarta.validation.constraints.NotBlank;

public record UnidadeMedidaRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotBlank(message = "Sigla é obrigatória") String sigla
) {
}
