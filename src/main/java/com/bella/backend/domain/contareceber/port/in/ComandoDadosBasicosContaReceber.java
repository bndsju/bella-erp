package com.bella.backend.domain.contareceber.port.in;

import java.util.UUID;

public record ComandoDadosBasicosContaReceber(
        String descricao,
        UUID clienteId,
        String observacoes
) {
}
