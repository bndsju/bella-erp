package com.bella.backend.domain.categoriafluxocaixa.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;

public class CategoriaFluxoCaixa {

    private final UUID id;
    private String nome;
    private StatusCategoriaFluxoCaixa status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private CategoriaFluxoCaixa(UUID id, String nome, StatusCategoriaFluxoCaixa status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = validarNome(nome);
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static CategoriaFluxoCaixa novo(String nome) {
        Instant agora = Instant.now();
        return new CategoriaFluxoCaixa(UUID.randomUUID(), nome, StatusCategoriaFluxoCaixa.ATIVO, agora, agora);
    }

    public static CategoriaFluxoCaixa existente(UUID id, String nome, StatusCategoriaFluxoCaixa status, Instant criadoEm,
                                       Instant atualizadoEm) {
        return new CategoriaFluxoCaixa(id, nome, status, criadoEm, atualizadoEm);
    }

    public void editar(String nome) {
        this.nome = validarNome(nome);
        this.atualizadoEm = Instant.now();
    }

    public void ativar() {
        this.status = StatusCategoriaFluxoCaixa.ATIVO;
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        this.status = StatusCategoriaFluxoCaixa.INATIVO;
        this.atualizadoEm = Instant.now();
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome da categoria de fluxo de caixa é obrigatório");
        }
        return nome;
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
