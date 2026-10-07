package com.bella.backend.adapter.out.persistence.compra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "compra_itens", schema = "bella")
public class CompraItemJpaEntity {

    @Id
    private UUID id;

    @Column(name = "produto_id", nullable = false)
    private UUID produtoId;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "custo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal desconto;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    protected CompraItemJpaEntity() {
    }

    public CompraItemJpaEntity(UUID id, UUID produtoId, BigDecimal quantidade, BigDecimal custoUnitario,
                                BigDecimal desconto, BigDecimal valorTotal) {
        this.id = id;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.desconto = desconto;
        this.valorTotal = valorTotal;
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

    public BigDecimal getValorTotal() {
        return valorTotal;
    }
}
