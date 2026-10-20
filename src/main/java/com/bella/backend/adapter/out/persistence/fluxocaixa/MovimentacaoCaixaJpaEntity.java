package com.bella.backend.adapter.out.persistence.fluxocaixa;

import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "movimentacoes_caixa", schema = "bella")
public class MovimentacaoCaixaJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacaoCaixa tipo;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(name = "categoria_id")
    private UUID categoriaId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SituacaoMovimentacaoCaixa situacao;

    @Column(name = "data_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(name = "data_efetiva")
    private LocalDate dataEfetiva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemMovimentacaoCaixa origem;

    @Column(name = "origem_referencia_id")
    private UUID origemReferenciaId;

    @Column(columnDefinition = "text")
    private String observacoes;

    @Column(nullable = false)
    private boolean cancelada;

    @Column(name = "motivo_cancelamento", columnDefinition = "text")
    private String motivoCancelamento;

    @Column(name = "cancelada_em")
    private Instant canceladaEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected MovimentacaoCaixaJpaEntity() {
    }

    public MovimentacaoCaixaJpaEntity(UUID id, TipoMovimentacaoCaixa tipo, String descricao, UUID categoriaId,
                                       BigDecimal valor, SituacaoMovimentacaoCaixa situacao,
                                       LocalDate dataPrevista, LocalDate dataEfetiva,
                                       OrigemMovimentacaoCaixa origem, UUID origemReferenciaId,
                                       String observacoes, boolean cancelada, String motivoCancelamento,
                                       Instant canceladaEm, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.tipo = tipo;
        this.descricao = descricao;
        this.categoriaId = categoriaId;
        this.valor = valor;
        this.situacao = situacao;
        this.dataPrevista = dataPrevista;
        this.dataEfetiva = dataEfetiva;
        this.origem = origem;
        this.origemReferenciaId = origemReferenciaId;
        this.observacoes = observacoes;
        this.cancelada = cancelada;
        this.motivoCancelamento = motivoCancelamento;
        this.canceladaEm = canceladaEm;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public TipoMovimentacaoCaixa getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public SituacaoMovimentacaoCaixa getSituacao() {
        return situacao;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public LocalDate getDataEfetiva() {
        return dataEfetiva;
    }

    public OrigemMovimentacaoCaixa getOrigem() {
        return origem;
    }

    public UUID getOrigemReferenciaId() {
        return origemReferenciaId;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
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
