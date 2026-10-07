package com.bella.backend.adapter.out.persistence.compra;

import com.bella.backend.domain.compra.model.StatusCompra;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "compras", schema = "bella")
public class CompraJpaEntity {

    @Id
    private UUID id;

    @Column(name = "fornecedor_id", nullable = false)
    private UUID fornecedorId;

    @Column(name = "data_compra", nullable = false)
    private LocalDate dataCompra;

    @Column(name = "previsao_recebimento")
    private LocalDate previsaoRecebimento;

    @Column(name = "data_recebimento")
    private LocalDate dataRecebimento;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "compra_id", nullable = false)
    @OrderColumn(name = "ordem")
    private List<CompraItemJpaEntity> itens = new ArrayList<>();

    @Column(name = "desconto_geral", nullable = false, precision = 12, scale = 2)
    private BigDecimal descontoGeral;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal frete;

    @Column(name = "outras_despesas", nullable = false, precision = 12, scale = 2)
    private BigDecimal outrasDespesas;

    @Column(name = "valor_produtos", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorProdutos;

    @Column(name = "desconto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal descontoTotal;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "condicao_pagamento", nullable = false, length = 100)
    private String condicaoPagamento;

    @Column(columnDefinition = "text")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCompra status;

    @Column(name = "motivo_cancelamento", columnDefinition = "text")
    private String motivoCancelamento;

    @Column(name = "confirmada_em")
    private Instant confirmadaEm;

    @Column(name = "cancelada_em")
    private Instant canceladaEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected CompraJpaEntity() {
    }

    public CompraJpaEntity(UUID id, UUID fornecedorId, LocalDate dataCompra, LocalDate previsaoRecebimento,
                            LocalDate dataRecebimento, List<CompraItemJpaEntity> itens, BigDecimal descontoGeral,
                            BigDecimal frete, BigDecimal outrasDespesas, BigDecimal valorProdutos,
                            BigDecimal descontoTotal, BigDecimal valorTotal, String condicaoPagamento,
                            String observacoes, StatusCompra status, String motivoCancelamento,
                            Instant confirmadaEm, Instant canceladaEm, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.fornecedorId = fornecedorId;
        this.dataCompra = dataCompra;
        this.previsaoRecebimento = previsaoRecebimento;
        this.dataRecebimento = dataRecebimento;
        this.itens = itens;
        this.descontoGeral = descontoGeral;
        this.frete = frete;
        this.outrasDespesas = outrasDespesas;
        this.valorProdutos = valorProdutos;
        this.descontoTotal = descontoTotal;
        this.valorTotal = valorTotal;
        this.condicaoPagamento = condicaoPagamento;
        this.observacoes = observacoes;
        this.status = status;
        this.motivoCancelamento = motivoCancelamento;
        this.confirmadaEm = confirmadaEm;
        this.canceladaEm = canceladaEm;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFornecedorId() {
        return fornecedorId;
    }

    public LocalDate getDataCompra() {
        return dataCompra;
    }

    public LocalDate getPrevisaoRecebimento() {
        return previsaoRecebimento;
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
    }

    public List<CompraItemJpaEntity> getItens() {
        return itens;
    }

    public BigDecimal getDescontoGeral() {
        return descontoGeral;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public BigDecimal getOutrasDespesas() {
        return outrasDespesas;
    }

    public BigDecimal getValorProdutos() {
        return valorProdutos;
    }

    public BigDecimal getDescontoTotal() {
        return descontoTotal;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getCondicaoPagamento() {
        return condicaoPagamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public StatusCompra getStatus() {
        return status;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public Instant getConfirmadaEm() {
        return confirmadaEm;
    }

    public Instant getCanceladaEm() {
        return canceladaEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
