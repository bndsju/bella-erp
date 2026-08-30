package com.bella.backend.domain.orcamento.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Comando compartilhado entre cadastro e edição de orçamento — ao contrário do
 * Cliente (PF/PJ), aqui não há variação de formato, então um único record basta
 * e evita a ambiguidade de "Comando" aninhado em duas interfaces implementadas
 * pelo mesmo service.
 */
public record ComandoOrcamento(
        UUID clienteId,
        List<ComandoItemOrcamento> itens,
        BigDecimal percentualDesconto,
        BigDecimal valorFrete,
        LocalDate prazoValidade,
        String condicaoPagamento,
        String observacoes
) {
}
