package com.bella.backend.adapter.in.web.unidademedida;

import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import com.bella.backend.domain.unidademedida.model.UnidadeMedida;

import java.time.Instant;
import java.util.UUID;

public record UnidadeMedidaResponse(
        UUID id,
        String nome,
        String sigla,
        StatusUnidadeMedida status,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static UnidadeMedidaResponse de(UnidadeMedida unidadeMedida) {
        return new UnidadeMedidaResponse(unidadeMedida.getId(), unidadeMedida.getNome(), unidadeMedida.getSigla(),
                unidadeMedida.getStatus(), unidadeMedida.getCriadoEm(), unidadeMedida.getAtualizadoEm());
    }
}
