package com.bella.backend.adapter.in.web.transportadora;

import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusTransportadoraRequest(
        @NotNull(message = "Status é obrigatório") StatusTransportadora status
) {
}
