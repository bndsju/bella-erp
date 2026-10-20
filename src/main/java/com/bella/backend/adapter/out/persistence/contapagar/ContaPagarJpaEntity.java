package com.bella.backend.adapter.out.persistence.contapagar;

import com.bella.backend.domain.contapagar.model.OrigemContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;
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
@Table(name = "contas_pagar", schema = "bella")
public class ContaPagarJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(name = "fornecedor_id")
    private UUID fornecedorId;

    @Column(name = "categoria_despesa_id", nullable = false)
    private UUID categoriaDespesaId;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(columnDefinition = "text")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemContaPagar origem;

    @Column(name = "origem_referencia_id")
    private UUID origemReferenciaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusContaPagar status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "conta_pagar_id", nullable = false)
    @OrderColumn(name = "ordem")
    private List<ParcelaContaPagarJpaEntity> parcelas = new ArrayList<>();

    @Column(name = "motivo_cancelamento", columnDefinition = "text")
    private String motivoCancelamento;

    @Column(name = "cancelada_em")
    private Instant canceladaEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected ContaPagarJpaEntity() {
    }

    public ContaPagarJpaEntity(UUID id, String descricao, UUID fornecedorId, UUID categoriaDespesaId,
                                BigDecimal valorTotal, LocalDate dataLancamento, String observacoes,
                                OrigemContaPagar origem, UUID origemReferenciaId, StatusContaPagar status,
                                List<ParcelaContaPagarJpaEntity> parcelas, String motivoCancelamento,
                                Instant canceladaEm, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.descricao = descricao;
        this.fornecedorId = fornecedorId;
        this.categoriaDespesaId = categoriaDespesaId;
        this.valorTotal = valorTotal;
        this.dataLancamento = dataLancamento;
        this.observacoes = observacoes;
        this.origem = origem;
        this.origemReferenciaId = origemReferenciaId;
        this.status = status;
        this.parcelas = parcelas;
        this.motivoCancelamento = motivoCancelamento;
        this.canceladaEm = canceladaEm;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getFornecedorId() {
        return fornecedorId;
    }

    public UUID getCategoriaDespesaId() {
        return categoriaDespesaId;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public OrigemContaPagar getOrigem() {
        return origem;
    }

    public UUID getOrigemReferenciaId() {
        return origemReferenciaId;
    }

    public StatusContaPagar getStatus() {
        return status;
    }

    public List<ParcelaContaPagarJpaEntity> getParcelas() {
        return parcelas;
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
