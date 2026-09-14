package com.bella.backend.adapter.in.web.pedido;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarPedidoDeOrcamentoRequest(
        @NotNull(message = "Orçamento é obrigatório") UUID orcamentoId,
        UUID transportadoraId
) {
}
