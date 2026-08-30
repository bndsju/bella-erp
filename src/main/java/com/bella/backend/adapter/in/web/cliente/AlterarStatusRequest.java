package com.bella.backend.adapter.in.web.cliente;

import com.bella.backend.domain.cliente.model.StatusCliente;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(
        @NotNull(message = "Status é obrigatório") StatusCliente status
) {
}
