package com.bella.backend.adapter.out.persistence.unidademedida;

import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "unidades_medida", schema = "bella")
public class UnidadeMedidaJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 10)
    private String sigla;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusUnidadeMedida status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected UnidadeMedidaJpaEntity() {
    }

    public UnidadeMedidaJpaEntity(UUID id, String nome, String sigla, StatusUnidadeMedida status, Instant criadoEm,
                                   Instant atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public StatusUnidadeMedida getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
