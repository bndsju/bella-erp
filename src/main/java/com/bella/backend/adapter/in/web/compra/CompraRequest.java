package com.bella.backend.adapter.in.web.compra;

import com.bella.backend.domain.compra.port.in.ComandoCompra;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CompraRequest(
        @NotNull(message = "Fornecedor é obrigatório") UUID fornecedorId,
        LocalDate previsaoRecebimento,
        @NotEmpty(message = "Compra precisa ter ao menos um item") @Valid List<CompraItemRequest> itens,
        @DecimalMin(value = "0", message = "Desconto não pode ser negativo") BigDecimal descontoGeral,
        @DecimalMin(value = "0", message = "Frete não pode ser negativo") BigDecimal frete,
        @DecimalMin(value = "0", message = "Outras despesas não podem ser negativas") BigDecimal outrasDespesas,
        @NotBlank(message = "Condição de pagamento é obrigatória") String condicaoPagamento,
        String observacoes
) {

    public ComandoCompra paraComando() {
        return new ComandoCompra(
                fornecedorId,
                previsaoRecebimento,
                itens.stream().map(CompraItemRequest::paraComando).toList(),
                descontoGeral,
                frete,
                outrasDespesas,
                condicaoPagamento,
                observacoes);
    }
}
