package com.bella.backend.adapter.in.web.contapagar;

import com.bella.backend.domain.contapagar.port.in.ComandoDadosBasicosContaPagar;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DadosBasicosContaPagarRequest(
        @NotBlank(message = "Descrição é obrigatória") String descricao,
        UUID fornecedorId,
        @NotNull(message = "Categoria de despesa é obrigatória") UUID categoriaDespesaId,
        String observacoes
) {

    public ComandoDadosBasicosContaPagar paraComando() {
        return new ComandoDadosBasicosContaPagar(descricao, fornecedorId, categoriaDespesaId, observacoes);
    }
}
