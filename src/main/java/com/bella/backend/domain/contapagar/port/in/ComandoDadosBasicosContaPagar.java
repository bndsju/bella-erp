package com.bella.backend.domain.contapagar.port.in;

import java.util.UUID;

public record ComandoDadosBasicosContaPagar(
        String descricao,
        UUID fornecedorId,
        UUID categoriaDespesaId,
        String observacoes
) {
}
