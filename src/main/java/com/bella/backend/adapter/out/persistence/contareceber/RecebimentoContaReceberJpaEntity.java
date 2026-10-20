package com.bella.backend.adapter.out.persistence.contareceber;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "recebimentos_conta_receber", schema = "bella")
public class RecebimentoContaReceberJpaEntity {

    @Id
    private UUID id;

    @Column(name = "valor_recebido", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorRecebido;

    @Column(name = "data_recebimento", nullable = false)
    private LocalDate dataRecebimento;

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

    protected RecebimentoContaReceberJpaEntity() {
    }

    public RecebimentoContaReceberJpaEntity(UUID id, BigDecimal valorRecebido, LocalDate dataRecebimento, BigDecimal juros,
                                         BigDecimal multa, BigDecimal desconto, String observacao,
                                         Instant criadoEm) {
        this.id = id;
        this.valorRecebido = valorRecebido;
        this.dataRecebimento = dataRecebimento;
        this.juros = juros;
        this.multa = multa;
        this.desconto = desconto;
        this.observacao = observacao;
        this.criadoEm = criadoEm;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getValorRecebido() {
        return valorRecebido;
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
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
