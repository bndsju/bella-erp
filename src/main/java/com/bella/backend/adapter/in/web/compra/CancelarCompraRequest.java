package com.bella.backend.adapter.in.web.compra;

import jakarta.validation.constraints.NotBlank;

public record CancelarCompraRequest(@NotBlank(message = "Motivo do cancelamento é obrigatório") String motivo) {
}
