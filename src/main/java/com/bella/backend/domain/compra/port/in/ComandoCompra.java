package com.bella.backend.domain.compra.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Comando compartilhado entre cadastro e edição de compra. Totais não fazem parte do comando:
 * são sempre calculados pelo domínio.
 */
public record ComandoCompra(
        UUID fornecedorId,
        LocalDate previsaoRecebimento,
        List<ComandoItemCompra> itens,
        BigDecimal descontoGeral,
        BigDecimal frete,
        BigDecimal outrasDespesas,
        String condicaoPagamento,
        String observacoes
) {
}
