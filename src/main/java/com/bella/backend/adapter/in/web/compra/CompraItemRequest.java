package com.bella.backend.adapter.in.web.compra;

import com.bella.backend.domain.compra.port.in.ComandoItemCompra;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CompraItemRequest(
        @NotNull(message = "Produto é obrigatório") UUID produtoId,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero") BigDecimal quantidade,
        @NotNull(message = "Custo unitário é obrigatório")
        @DecimalMin(value = "0", message = "Custo unitário não pode ser negativo") BigDecimal custoUnitario,
        @DecimalMin(value = "0", message = "Desconto do item não pode ser negativo") BigDecimal desconto
) {

    public ComandoItemCompra paraComando() {
        return new ComandoItemCompra(produtoId, quantidade, custoUnitario, desconto);
    }
}
