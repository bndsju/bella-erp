package com.bella.backend.adapter.in.web.fluxocaixa;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.fluxocaixa.model.ResumoCategoriaCaixa;

import java.util.UUID;

/** categoriaId e categoriaNome nulos representam as movimentações sem categoria. */
public record ResumoCategoriaCaixaResponse(UUID categoriaId, String categoriaNome, ResumoCaixaResponse resumo) {

    public static ResumoCategoriaCaixaResponse de(ResumoCategoriaCaixa item, CategoriaFluxoCaixa categoria) {
        return new ResumoCategoriaCaixaResponse(
                categoria == null ? null : categoria.getId(),
                categoria == null ? null : categoria.getNome(),
                ResumoCaixaResponse.de(item.resumo()));
    }
}
