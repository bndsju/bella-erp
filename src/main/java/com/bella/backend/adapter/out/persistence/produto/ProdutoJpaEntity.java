package com.bella.backend.adapter.out.persistence.produto;

import com.bella.backend.domain.produto.model.StatusProduto;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "produtos", schema = "bella")
public class ProdutoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "codigo_interno", nullable = false, length = 50)
    private String codigoInterno;

    @Column(name = "codigo_barras", length = 50)
    private String codigoBarras;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "categoria_id", nullable = false)
    private UUID categoriaId;

    @Column(name = "unidade_medida_id", nullable = false)
    private UUID unidadeMedidaId;

    @Column(name = "preco_custo", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoCusto;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueMinimo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusProduto status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected ProdutoJpaEntity() {
    }

    public ProdutoJpaEntity(UUID id, String codigoInterno, String codigoBarras, String nome, String descricao,
                             UUID categoriaId, UUID unidadeMedidaId, BigDecimal precoCusto, BigDecimal precoVenda,
                             BigDecimal estoqueMinimo, StatusProduto status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.codigoInterno = codigoInterno;
        this.codigoBarras = codigoBarras;
        this.nome = nome;
        this.descricao = descricao;
        this.categoriaId = categoriaId;
        this.unidadeMedidaId = unidadeMedidaId;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.estoqueMinimo = estoqueMinimo;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public UUID getUnidadeMedidaId() {
        return unidadeMedidaId;
    }

    public BigDecimal getPrecoCusto() {
        return precoCusto;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public BigDecimal getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public StatusProduto getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
