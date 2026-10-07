package com.bella.backend.domain.fornecedor.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;

public class Fornecedor {

    private final UUID id;
    private String nome;
    private String telefone;
    private StatusFornecedor status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Fornecedor(UUID id, String nome, String telefone, StatusFornecedor status, Instant criadoEm,
                            Instant atualizadoEm) {
        this.id = id;
        this.nome = validarNome(nome);
        this.telefone = telefone;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Fornecedor novo(String nome, String telefone) {
        Instant agora = Instant.now();
        return new Fornecedor(UUID.randomUUID(), nome, telefone, StatusFornecedor.ATIVO, agora, agora);
    }

    public static Fornecedor existente(UUID id, String nome, String telefone, StatusFornecedor status,
                                            Instant criadoEm, Instant atualizadoEm) {
        return new Fornecedor(id, nome, telefone, status, criadoEm, atualizadoEm);
    }

    public void editar(String nome, String telefone) {
        this.nome = validarNome(nome);
        this.telefone = telefone;
        this.atualizadoEm = Instant.now();
    }

    public void ativar() {
        this.status = StatusFornecedor.ATIVO;
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        this.status = StatusFornecedor.INATIVO;
        this.atualizadoEm = Instant.now();
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do fornecedor é obrigatório");
        }
        return nome;
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

    public StatusFornecedor getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
