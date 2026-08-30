package com.bella.backend.domain.unidademedida.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;

public class UnidadeMedida {

    private final UUID id;
    private String nome;
    private String sigla;
    private StatusUnidadeMedida status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private UnidadeMedida(UUID id, String nome, String sigla, StatusUnidadeMedida status, Instant criadoEm,
                           Instant atualizadoEm) {
        this.id = id;
        this.nome = validarNome(nome);
        this.sigla = validarSigla(sigla);
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static UnidadeMedida novo(String nome, String sigla) {
        Instant agora = Instant.now();
        return new UnidadeMedida(UUID.randomUUID(), nome, sigla, StatusUnidadeMedida.ATIVO, agora, agora);
    }

    public static UnidadeMedida existente(UUID id, String nome, String sigla, StatusUnidadeMedida status,
                                           Instant criadoEm, Instant atualizadoEm) {
        return new UnidadeMedida(id, nome, sigla, status, criadoEm, atualizadoEm);
    }

    public void editar(String nome, String sigla) {
        this.nome = validarNome(nome);
        this.sigla = validarSigla(sigla);
        this.atualizadoEm = Instant.now();
    }

    public void ativar() {
        this.status = StatusUnidadeMedida.ATIVO;
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        this.status = StatusUnidadeMedida.INATIVO;
        this.atualizadoEm = Instant.now();
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome da unidade de medida é obrigatório");
        }
        return nome;
    }

    private static String validarSigla(String sigla) {
        if (sigla == null || sigla.isBlank()) {
            throw new RegraDeNegocioException("Sigla da unidade de medida é obrigatória");
        }
        return sigla;
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
