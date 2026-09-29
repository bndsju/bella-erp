package com.bella.backend.adapter.out.persistence.estoque;

import com.bella.backend.domain.estoque.model.OrigemMovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
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
@Table(name = "movimentacoes_estoque", schema = "bella")
public class MovimentacaoEstoqueJpaEntity {

    @Id
    private UUID id;

    @Column(name = "produto_id", nullable = false)
    private UUID produtoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentacaoEstoque tipo;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidade;

    @Column(nullable = false, length = 200)
    private String motivo;

    @Column(columnDefinition = "text")
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemMovimentacaoEstoque origem;

    @Column(name = "origem_operacao_id")
    private UUID origemOperacaoId;

    @Column(name = "usuario_responsavel", length = 150)
    private String usuarioResponsavel;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected MovimentacaoEstoqueJpaEntity() {
    }

    public MovimentacaoEstoqueJpaEntity(UUID id, UUID produtoId, TipoMovimentacaoEstoque tipo, BigDecimal quantidade,
                                         String motivo, String observacao, OrigemMovimentacaoEstoque origem,
                                         UUID origemOperacaoId, String usuarioResponsavel, Instant dataHora) {
        this.id = id;
        this.produtoId = produtoId;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.motivo = motivo;
        this.observacao = observacao;
        this.origem = origem;
        this.origemOperacaoId = origemOperacaoId;
        this.usuarioResponsavel = usuarioResponsavel;
        this.dataHora = dataHora;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public TipoMovimentacaoEstoque getTipo() {
        return tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getObservacao() {
        return observacao;
    }

    public OrigemMovimentacaoEstoque getOrigem() {
        return origem;
    }

    public UUID getOrigemOperacaoId() {
        return origemOperacaoId;
    }

    public String getUsuarioResponsavel() {
        return usuarioResponsavel;
    }

    public Instant getDataHora() {
        return dataHora;
    }
}
