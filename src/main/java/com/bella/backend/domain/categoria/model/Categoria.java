package com.bella.backend.domain.categoria.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;

public class Categoria {

    private final UUID id;
    private String nome;
    private StatusCategoria status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Categoria(UUID id, String nome, StatusCategoria status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = validarNome(nome);
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Categoria novo(String nome) {
        Instant agora = Instant.now();
        return new Categoria(UUID.randomUUID(), nome, StatusCategoria.ATIVO, agora, agora);
    }

    public static Categoria existente(UUID id, String nome, StatusCategoria status, Instant criadoEm,
                                       Instant atualizadoEm) {
        return new Categoria(id, nome, status, criadoEm, atualizadoEm);
    }

    public void editar(String nome) {
        this.nome = validarNome(nome);
        this.atualizadoEm = Instant.now();
    }

    public void ativar() {
        this.status = StatusCategoria.ATIVO;
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        this.status = StatusCategoria.INATIVO;
        this.atualizadoEm = Instant.now();
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome da categoria é obrigatório");
        }
        return nome;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public StatusCategoria getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
