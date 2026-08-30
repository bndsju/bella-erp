package com.bella.backend.adapter.out.persistence.cliente;

import com.bella.backend.domain.cliente.model.StatusCliente;
import com.bella.backend.domain.cliente.model.TipoPessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clientes", schema = "bella")
public class ClienteJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false, length = 2)
    private TipoPessoa tipoPessoa;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false, length = 20)
    private String celular;

    @Column(nullable = false, length = 150)
    private String email;

    @Embedded
    private EnderecoEmbeddable endereco;

    @Column(columnDefinition = "text")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusCliente status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected ClienteJpaEntity() {
    }

    public ClienteJpaEntity(UUID id, TipoPessoa tipoPessoa, String telefone, String celular, String email,
                             EnderecoEmbeddable endereco, String observacoes, StatusCliente status,
                             Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.tipoPessoa = tipoPessoa;
        this.telefone = telefone;
        this.celular = celular;
        this.email = email;
        this.endereco = endereco;
        this.observacoes = observacoes;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getCelular() {
        return celular;
    }

    public String getEmail() {
        return email;
    }

    public EnderecoEmbeddable getEndereco() {
        return endereco;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public StatusCliente getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
