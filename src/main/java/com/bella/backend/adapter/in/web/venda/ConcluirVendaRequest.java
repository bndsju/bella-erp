package com.bella.backend.adapter.in.web.venda;

import com.bella.backend.domain.venda.model.FormaPagamento;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConcluirVendaRequest(
        @NotNull(message = "Pedido é obrigatório") UUID pedidoId,
        @NotNull(message = "Forma de pagamento é obrigatória") FormaPagamento formaPagamento
) {
}
