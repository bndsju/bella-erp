package com.bella.backend.domain.estoque.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Comando compartilhado pelas quatro movimentações manuais (entrada, saída, ajuste de entrada
 * e ajuste de saída), que têm o mesmo formato. Declarado como record de nível superior — e não
 * aninhado em cada use case — porque uma única classe de serviço implementa as quatro
 * interfaces, e um Comando aninhado por interface geraria referência ambígua ao tipo.
 */
public record ComandoMovimentacaoManual(
        UUID produtoId,
        BigDecimal quantidade,
        String motivo,
        String observacao,
        String usuarioResponsavel
) {
}
