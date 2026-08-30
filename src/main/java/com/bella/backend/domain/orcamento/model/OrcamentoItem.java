package com.bella.backend.domain.orcamento.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class OrcamentoItem {

    private final UUID id;
    private final UUID produtoId;
    private final BigDecimal quantidade;
    private final BigDecimal valorUnitario;

    private OrcamentoItem(UUID id, UUID produtoId, BigDecimal quantidade, BigDecimal valorUnitario) {
        this.id = id;
        this.produtoId = validarProdutoId(produtoId);
        this.quantidade = validarQuantidade(quantidade);
        this.valorUnitario = validarValorUnitario(valorUnitario);
    }

    public static OrcamentoItem novo(UUID produtoId, BigDecimal quantidade, BigDecimal valorUnitario) {
        return new OrcamentoItem(UUID.randomUUID(), produtoId, quantidade, valorUnitario);
    }

    public static OrcamentoItem existente(UUID id, UUID produtoId, BigDecimal quantidade, BigDecimal valorUnitario) {
        return new OrcamentoItem(id, produtoId, quantidade, valorUnitario);
    }

    public BigDecimal getSubtotal() {
        return valorUnitario.multiply(quantidade).setScale(2, RoundingMode.HALF_UP);
    }

    private static UUID validarProdutoId(UUID produtoId) {
        if (produtoId == null) {
            throw new RegraDeNegocioException("Produto do item é obrigatório");
        }
        return produtoId;
    }

    private static BigDecimal validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraDeNegocioException("Quantidade do item deve ser maior que zero");
        }
        return quantidade;
    }

    private static BigDecimal validarValorUnitario(BigDecimal valorUnitario) {
        if (valorUnitario == null || valorUnitario.signum() < 0) {
            throw new RegraDeNegocioException("Valor unitário do item não pode ser negativo");
        }
        return valorUnitario;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }
}
