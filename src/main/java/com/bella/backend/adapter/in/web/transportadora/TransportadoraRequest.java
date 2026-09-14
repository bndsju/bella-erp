package com.bella.backend.adapter.in.web.transportadora;

import jakarta.validation.constraints.NotBlank;

public record TransportadoraRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        String telefone
) {
}
