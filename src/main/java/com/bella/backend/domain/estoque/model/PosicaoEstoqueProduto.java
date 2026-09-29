package com.bella.backend.domain.estoque.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Retrato de consulta do estoque de um produto: junta o {@link SaldoEstoque} com o estoque
 * mínimo cadastrado no Produto. Não é um agregado — é montado pela camada de aplicação para
 * responder às consultas de saldo.
 */
public record PosicaoEstoqueProduto(
        UUID produtoId,
        BigDecimal estoqueFisico,
        BigDecimal quantidadeReservada,
        BigDecimal quantidadeDisponivel,
        BigDecimal estoqueMinimo
) {

    public static PosicaoEstoqueProduto de(SaldoEstoque saldo, BigDecimal estoqueMinimo) {
        return new PosicaoEstoqueProduto(
                saldo.getProdutoId(), saldo.getEstoqueFisico(), saldo.getQuantidadeReservada(),
                saldo.getDisponivel(), estoqueMinimo);
    }

    public boolean abaixoDoMinimo() {
        return quantidadeDisponivel.compareTo(estoqueMinimo) <= 0;
    }
}
