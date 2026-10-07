package com.bella.backend.adapter.in.web.contapagar;

import jakarta.validation.constraints.NotBlank;

public record CancelarContaPagarRequest(@NotBlank(message = "Motivo do cancelamento é obrigatório") String motivo) {
}
