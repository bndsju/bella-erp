package com.bella.backend.adapter.in.web.orcamento;

import com.bella.backend.domain.orcamento.port.in.ComandoOrcamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OrcamentoRequest(
        @NotNull(message = "Cliente é obrigatório") UUID clienteId,
        @NotEmpty(message = "Orçamento precisa ter ao menos um item") @Valid List<OrcamentoItemRequest> itens,
        @NotNull(message = "Percentual de desconto é obrigatório")
        @DecimalMin(value = "0", message = "Percentual de desconto deve estar entre 0 e 100")
        @DecimalMax(value = "100", message = "Percentual de desconto deve estar entre 0 e 100") BigDecimal percentualDesconto,
        @NotNull(message = "Valor do frete é obrigatório")
        @DecimalMin(value = "0", message = "Valor do frete não pode ser negativo") BigDecimal valorFrete,
        @NotNull(message = "Prazo de validade é obrigatório")
        @FutureOrPresent(message = "Prazo de validade não pode ser uma data passada") LocalDate prazoValidade,
        @NotBlank(message = "Condição de pagamento é obrigatória") String condicaoPagamento,
        String observacoes
) {

    public ComandoOrcamento paraComando() {
        return new ComandoOrcamento(
                clienteId,
                itens.stream().map(OrcamentoItemRequest::paraComando).toList(),
                percentualDesconto,
                valorFrete,
                prazoValidade,
                condicaoPagamento,
                observacoes);
    }
}
