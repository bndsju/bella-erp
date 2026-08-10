package com.bella.backend.adapter.in.web.produto;

import com.bella.backend.domain.produto.port.in.EditarProdutoUseCase;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoEdicaoRequest(
        String codigoBarras,
        @NotBlank(message = "Nome é obrigatório") String nome,
        String descricao,
        @NotNull(message = "Categoria é obrigatória") UUID categoriaId,
        @NotNull(message = "Unidade de medida é obrigatória") UUID unidadeMedidaId,
        @NotNull(message = "Preço de custo é obrigatório")
        @DecimalMin(value = "0", message = "Preço de custo não pode ser negativo") BigDecimal precoCusto,
        @NotNull(message = "Preço de venda é obrigatório")
        @DecimalMin(value = "0", message = "Preço de venda não pode ser negativo") BigDecimal precoVenda,
        @NotNull(message = "Estoque mínimo é obrigatório")
        @DecimalMin(value = "0", message = "Estoque mínimo não pode ser negativo") BigDecimal estoqueMinimo
) {

    public EditarProdutoUseCase.Comando paraComando() {
        return new EditarProdutoUseCase.Comando(codigoBarras, nome, descricao, categoriaId, unidadeMedidaId,
                precoCusto, precoVenda, estoqueMinimo);
    }
}
