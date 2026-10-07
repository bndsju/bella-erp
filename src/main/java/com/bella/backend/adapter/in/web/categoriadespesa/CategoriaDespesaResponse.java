package com.bella.backend.adapter.in.web.categoriadespesa;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;

import java.time.Instant;
import java.util.UUID;

public record CategoriaDespesaResponse(
        UUID id,
        String nome,
        StatusCategoriaDespesa status,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static CategoriaDespesaResponse de(CategoriaDespesa categoriaDespesa) {
        return new CategoriaDespesaResponse(categoriaDespesa.getId(), categoriaDespesa.getNome(), categoriaDespesa.getStatus(),
                categoriaDespesa.getCriadoEm(), categoriaDespesa.getAtualizadoEm());
    }
}
