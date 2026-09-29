package com.bella.backend.adapter.out.persistence.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saldos_estoque", schema = "bella")
public class SaldoEstoqueJpaEntity {

    @Id
    @Column(name = "produto_id")
    private UUID produtoId;

    @Column(name = "estoque_fisico", nullable = false, precision = 14, scale = 3)
    private BigDecimal estoqueFisico;

    @Column(name = "quantidade_reservada", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadeReservada;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected SaldoEstoqueJpaEntity() {
    }

    public SaldoEstoqueJpaEntity(UUID produtoId, BigDecimal estoqueFisico, BigDecimal quantidadeReservada,
                                  Instant atualizadoEm) {
        this.produtoId = produtoId;
        this.estoqueFisico = estoqueFisico;
        this.quantidadeReservada = quantidadeReservada;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public BigDecimal getEstoqueFisico() {
        return estoqueFisico;
    }

    public BigDecimal getQuantidadeReservada() {
        return quantidadeReservada;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
