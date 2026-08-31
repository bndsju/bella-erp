package com.bella.backend.domain.transportadora.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;

public class Transportadora {

    private final UUID id;
    private String nome;
    private String telefone;
    private StatusTransportadora status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Transportadora(UUID id, String nome, String telefone, StatusTransportadora status, Instant criadoEm,
                            Instant atualizadoEm) {
        this.id = id;
        this.nome = validarNome(nome);
        this.telefone = telefone;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Transportadora novo(String nome, String telefone) {
        Instant agora = Instant.now();
        return new Transportadora(UUID.randomUUID(), nome, telefone, StatusTransportadora.ATIVO, agora, agora);
    }

    public static Transportadora existente(UUID id, String nome, String telefone, StatusTransportadora status,
                                            Instant criadoEm, Instant atualizadoEm) {
        return new Transportadora(id, nome, telefone, status, criadoEm, atualizadoEm);
    }

    public void editar(String nome, String telefone) {
        this.nome = validarNome(nome);
        this.telefone = telefone;
        this.atualizadoEm = Instant.now();
    }

    public void ativar() {
        this.status = StatusTransportadora.ATIVO;
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        this.status = StatusTransportadora.INATIVO;
        this.atualizadoEm = Instant.now();
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome da transportadora é obrigatório");
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
