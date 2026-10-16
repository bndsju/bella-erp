package com.bella.backend.adapter.in.web.contareceber;

import com.bella.backend.domain.contareceber.port.in.ComandoDadosBasicosContaReceber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DadosBasicosContaReceberRequest(
        @NotBlank(message = "Descrição é obrigatória") String descricao,
        UUID clienteId,
        String observacoes
) {

    public ComandoDadosBasicosContaReceber paraComando() {
        return new ComandoDadosBasicosContaReceber(descricao, clienteId, observacoes);
    }
}
