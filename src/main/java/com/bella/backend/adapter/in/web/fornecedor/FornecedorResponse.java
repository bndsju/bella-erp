package com.bella.backend.adapter.in.web.fornecedor;

import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import com.bella.backend.domain.fornecedor.model.Fornecedor;

import java.time.Instant;
import java.util.UUID;

public record FornecedorResponse(
        UUID id,
        String nome,
        String telefone,
        StatusFornecedor status,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static FornecedorResponse de(Fornecedor fornecedor) {
        return new FornecedorResponse(fornecedor.getId(), fornecedor.getNome(),
                fornecedor.getTelefone(), fornecedor.getStatus(), fornecedor.getCriadoEm(),
                fornecedor.getAtualizadoEm());
    }
}
