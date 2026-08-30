package com.bella.backend.adapter.out.persistence.orcamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orcamento_itens", schema = "bella")
public class OrcamentoItemJpaEntity {

    @Id
    private UUID id;

    @Column(name = "produto_id", nullable = false)
    private UUID produtoId;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    protected OrcamentoItemJpaEntity() {
    }

    public OrcamentoItemJpaEntity(UUID id, UUID produtoId, BigDecimal quantidade, BigDecimal valorUnitario,
                                   BigDecimal subtotal) {
        this.id = id;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.subtotal = subtotal;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
