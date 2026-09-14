package com.bella.backend.adapter.out.persistence.venda;

import com.bella.backend.domain.venda.model.FormaPagamento;
import com.bella.backend.domain.venda.model.StatusVenda;
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
@Table(name = "vendas", schema = "bella")
public class VendaJpaEntity {

    @Id
    private UUID id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private UUID pedidoId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "venda_id", nullable = false)
    @OrderColumn(name = "ordem")
    private List<VendaItemJpaEntity> itens = new ArrayList<>();

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

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Column(name = "condicao_pagamento", nullable = false, length = 100)
    private String condicaoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusVenda status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected VendaJpaEntity() {
    }

    public VendaJpaEntity(UUID id, UUID pedidoId, UUID clienteId, List<VendaItemJpaEntity> itens,
                           BigDecimal percentualDesconto, BigDecimal valorFrete, BigDecimal subtotal,
                           BigDecimal valorDesconto, BigDecimal valorTotal, FormaPagamento formaPagamento,
                           String condicaoPagamento, StatusVenda status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.itens = itens;
        this.percentualDesconto = percentualDesconto;
        this.valorFrete = valorFrete;
        this.subtotal = subtotal;
        this.valorDesconto = valorDesconto;
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.condicaoPagamento = condicaoPagamento;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public List<VendaItemJpaEntity> getItens() {
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

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public String getCondicaoPagamento() {
        return condicaoPagamento;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
