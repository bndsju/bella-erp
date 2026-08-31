package com.bella.backend.adapter.out.persistence.pedido;

import com.bella.backend.domain.pedido.model.StatusPedido;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedidos", schema = "bella")
public class PedidoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "orcamento_origem_id")
    private UUID orcamentoOrigemId;

    @Column(name = "transportadora_id")
    private UUID transportadoraId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false)
    @OrderColumn(name = "ordem")
    private List<PedidoItemJpaEntity> itens = new ArrayList<>();

    @Column(name = "percentual_desconto", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualDesconto;

    @Column(name = "valor_frete", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorFrete;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "valor_desconto", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorDesconto;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "condicao_pagamento", nullable = false, length = 100)
    private String condicaoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected PedidoJpaEntity() {
    }

    public PedidoJpaEntity(UUID id, UUID clienteId, UUID orcamentoOrigemId, UUID transportadoraId,
                            List<PedidoItemJpaEntity> itens, BigDecimal percentualDesconto, BigDecimal valorFrete,
                            BigDecimal subtotal, BigDecimal valorDesconto, BigDecimal valorTotal,
                            String condicaoPagamento, StatusPedido status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.clienteId = clienteId;
        this.orcamentoOrigemId = orcamentoOrigemId;
        this.transportadoraId = transportadoraId;
        this.itens = itens;
        this.percentualDesconto = percentualDesconto;
        this.valorFrete = valorFrete;
        this.subtotal = subtotal;
        this.valorDesconto = valorDesconto;
        this.valorTotal = valorTotal;
        this.condicaoPagamento = condicaoPagamento;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getOrcamentoOrigemId() {
        return orcamentoOrigemId;
    }

    public UUID getTransportadoraId() {
        return transportadoraId;
    }

    public List<PedidoItemJpaEntity> getItens() {
        return itens;
    }

    public BigDecimal getPercentualDesconto() {
        return percentualDesconto;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getCondicaoPagamento() {
        return condicaoPagamento;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
