package com.bella.backend.adapter.in.web.contareceber;

import jakarta.validation.constraints.NotBlank;

public record CancelarContaReceberRequest(@NotBlank(message = "Motivo do cancelamento é obrigatório") String motivo) {
}
