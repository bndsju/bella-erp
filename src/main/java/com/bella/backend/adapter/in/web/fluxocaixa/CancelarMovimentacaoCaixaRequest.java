package com.bella.backend.adapter.in.web.fluxocaixa;

import jakarta.validation.constraints.NotBlank;

public record CancelarMovimentacaoCaixaRequest(
        @NotBlank(message = "Motivo do cancelamento é obrigatório") String motivo
) {
}
