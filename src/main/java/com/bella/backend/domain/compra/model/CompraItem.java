package com.bella.backend.domain.compra.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class CompraItem {

    private final UUID id;
    private final UUID produtoId;
    private final BigDecimal quantidade;
    private final BigDecimal custoUnitario;
    private final BigDecimal desconto;

    private CompraItem(UUID id, UUID produtoId, BigDecimal quantidade, BigDecimal custoUnitario,
                        BigDecimal desconto) {
        this.id = id;
        this.produtoId = validarProdutoId(produtoId);
        this.quantidade = validarQuantidade(quantidade);
        this.custoUnitario = validarCustoUnitario(custoUnitario);
        this.desconto = validarDesconto(desconto, this.quantidade, this.custoUnitario);
    }

    public static CompraItem novo(UUID produtoId, BigDecimal quantidade, BigDecimal custoUnitario,
                                   BigDecimal desconto) {
        return new CompraItem(UUID.randomUUID(), produtoId, quantidade, custoUnitario, desconto);
    }

    public static CompraItem existente(UUID id, UUID produtoId, BigDecimal quantidade, BigDecimal custoUnitario,
                                        BigDecimal desconto) {
        return new CompraItem(id, produtoId, quantidade, custoUnitario, desconto);
    }

    public BigDecimal getValorBruto() {
        return custoUnitario.multiply(quantidade).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getValorTotal() {
        return getValorBruto().subtract(desconto);
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

    private static BigDecimal validarCustoUnitario(BigDecimal custoUnitario) {
        if (custoUnitario == null || custoUnitario.signum() < 0) {
            throw new RegraDeNegocioException("Custo unitário do item não pode ser negativo");
        }
        return custoUnitario;
    }

    private static BigDecimal validarDesconto(BigDecimal desconto, BigDecimal quantidade, BigDecimal custoUnitario) {
        BigDecimal valor = desconto == null ? BigDecimal.ZERO : desconto;
        if (valor.signum() < 0) {
            throw new RegraDeNegocioException("Desconto do item não pode ser negativo");
        }
        BigDecimal bruto = custoUnitario.multiply(quantidade).setScale(2, RoundingMode.HALF_UP);
        if (valor.compareTo(bruto) > 0) {
            throw new RegraDeNegocioException("Desconto do item não pode ser maior que o valor do item");
        }
        return valor;
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

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }
}
