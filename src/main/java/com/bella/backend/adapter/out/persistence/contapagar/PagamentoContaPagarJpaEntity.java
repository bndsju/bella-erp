package com.bella.backend.adapter.out.persistence.contapagar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pagamentos_conta_pagar", schema = "bella")
public class PagamentoContaPagarJpaEntity {

    @Id
    private UUID id;

    @Column(name = "valor_pago", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal juros;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal multa;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal desconto;

    @Column(columnDefinition = "text")
    private String observacao;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected PagamentoContaPagarJpaEntity() {
    }

    public PagamentoContaPagarJpaEntity(UUID id, BigDecimal valorPago, LocalDate dataPagamento, BigDecimal juros,
                                         BigDecimal multa, BigDecimal desconto, String observacao,
                                         Instant criadoEm) {
        this.id = id;
        this.valorPago = valorPago;
        this.dataPagamento = dataPagamento;
        this.juros = juros;
        this.multa = multa;
        this.desconto = desconto;
        this.observacao = observacao;
        this.criadoEm = criadoEm;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public BigDecimal getJuros() {
        return juros;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public String getObservacao() {
        return observacao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
