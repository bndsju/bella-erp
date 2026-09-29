package com.bella.backend.adapter.in.web.estoque;

import com.bella.backend.domain.estoque.port.in.ComandoMovimentacaoManual;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record MovimentacaoManualRequest(
        @NotNull(message = "Produto é obrigatório") UUID produtoId,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero") BigDecimal quantidade,
        @NotBlank(message = "Motivo é obrigatório") String motivo,
        String observacao,
        String usuarioResponsavel
) {

    public ComandoMovimentacaoManual paraComando() {
        return new ComandoMovimentacaoManual(produtoId, quantidade, motivo, observacao, usuarioResponsavel);
    }
}
