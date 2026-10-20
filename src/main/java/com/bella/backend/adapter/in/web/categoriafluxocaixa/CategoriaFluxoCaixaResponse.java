package com.bella.backend.adapter.in.web.categoriafluxocaixa;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;

import java.time.Instant;
import java.util.UUID;

public record CategoriaFluxoCaixaResponse(
        UUID id,
        String nome,
        StatusCategoriaFluxoCaixa status,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static CategoriaFluxoCaixaResponse de(CategoriaFluxoCaixa categoriaFluxoCaixa) {
        return new CategoriaFluxoCaixaResponse(categoriaFluxoCaixa.getId(), categoriaFluxoCaixa.getNome(), categoriaFluxoCaixa.getStatus(),
                categoriaFluxoCaixa.getCriadoEm(), categoriaFluxoCaixa.getAtualizadoEm());
    }
}
