package com.bella.backend.domain.fluxocaixa.model;

import java.util.UUID;

/** Totais de uma categoria; categoriaId nulo agrupa as movimentações sem categoria. */
public record ResumoCategoriaCaixa(UUID categoriaId, ResumoCaixa resumo) {
}
