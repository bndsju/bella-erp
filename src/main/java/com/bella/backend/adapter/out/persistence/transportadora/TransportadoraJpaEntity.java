package com.bella.backend.adapter.out.persistence.transportadora;

import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transportadoras", schema = "bella")
public class TransportadoraJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusTransportadora status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected TransportadoraJpaEntity() {
    }

    public TransportadoraJpaEntity(UUID id, String nome, String telefone, StatusTransportadora status,
                                    Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
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

    public String getTelefone() {
        return telefone;
    }

    public StatusTransportadora getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
