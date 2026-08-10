package com.bella.backend.adapter.in.web.categoria;

import com.bella.backend.domain.categoria.model.Categoria;
import com.bella.backend.domain.categoria.model.StatusCategoria;

import java.time.Instant;
import java.util.UUID;

public record CategoriaResponse(
        UUID id,
        String nome,
        StatusCategoria status,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getStatus(),
                categoria.getCriadoEm(), categoria.getAtualizadoEm());
    }
}
