package com.bella.backend.adapter.out.persistence.contareceber;

import com.bella.backend.domain.contareceber.model.OrigemContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;
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
@Table(name = "contas_receber", schema = "bella")
public class ContaReceberJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(name = "cliente_id")
    private UUID clienteId;


    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(columnDefinition = "text")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemContaReceber origem;

    @Column(name = "origem_referencia_id")
    private UUID origemReferenciaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusContaReceber status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "conta_receber_id", nullable = false)
    @OrderColumn(name = "ordem")
    private List<ParcelaContaReceberJpaEntity> parcelas = new ArrayList<>();

    @Column(name = "motivo_cancelamento", columnDefinition = "text")
    private String motivoCancelamento;

    @Column(name = "cancelada_em")
    private Instant canceladaEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected ContaReceberJpaEntity() {
    }

    public ContaReceberJpaEntity(UUID id, String descricao, UUID clienteId,
                                BigDecimal valorTotal, LocalDate dataLancamento, String observacoes,
                                OrigemContaReceber origem, UUID origemReferenciaId, StatusContaReceber status,
                                List<ParcelaContaReceberJpaEntity> parcelas, String motivoCancelamento,
                                Instant canceladaEm, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.descricao = descricao;
        this.clienteId = clienteId;
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

    public UUID getClienteId() {
        return clienteId;
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

    public OrigemContaReceber getOrigem() {
        return origem;
    }

    public UUID getOrigemReferenciaId() {
        return origemReferenciaId;
    }

    public StatusContaReceber getStatus() {
        return status;
    }

    public List<ParcelaContaReceberJpaEntity> getParcelas() {
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
