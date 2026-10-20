package com.bella.backend.adapter.out.persistence.categoriafluxocaixa;

import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categorias_fluxo_caixa", schema = "bella")
public class CategoriaFluxoCaixaJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusCategoriaFluxoCaixa status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected CategoriaFluxoCaixaJpaEntity() {
    }

    public CategoriaFluxoCaixaJpaEntity(UUID id, String nome, StatusCategoriaFluxoCaixa status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = nome;
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

    public StatusCategoriaFluxoCaixa getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
