package com.bella.backend.adapter.in.web.orcamento;

import com.bella.backend.domain.orcamento.port.in.ComandoItemOrcamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OrcamentoItemRequest(
        @NotNull(message = "Produto é obrigatório") UUID produtoId,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero") BigDecimal quantidade,
        @NotNull(message = "Valor unitário é obrigatório")
        @DecimalMin(value = "0", message = "Valor unitário não pode ser negativo") BigDecimal valorUnitario
) {

    public ComandoItemOrcamento paraComando() {
        return new ComandoItemOrcamento(produtoId, quantidade, valorUnitario);
    }
}
