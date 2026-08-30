package com.bella.backend.domain.cliente.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;

public abstract sealed class Cliente permits ClientePessoaFisica, ClientePessoaJuridica {

    private final UUID id;
    private String telefone;
    private String celular;
    private String email;
    private Endereco endereco;
    private String observacoes;
    private StatusCliente status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    protected Cliente(UUID id, String celular, String email, String telefone, Endereco endereco,
                       String observacoes, StatusCliente status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.celular = validarCelular(celular);
        this.email = validarEmail(email);
        this.telefone = telefone;
        this.endereco = validarEndereco(endereco);
        this.observacoes = observacoes;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    protected void atualizarDadosComuns(String celular, String email, String telefone, Endereco endereco,
                                         String observacoes) {
        this.celular = validarCelular(celular);
        this.email = validarEmail(email);
        this.telefone = telefone;
        this.endereco = validarEndereco(endereco);
        this.observacoes = observacoes;
        this.atualizadoEm = Instant.now();
    }

    public void ativar() {
        this.status = StatusCliente.ATIVO;
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        this.status = StatusCliente.INATIVO;
        this.atualizadoEm = Instant.now();
    }

    public abstract TipoPessoa getTipoPessoa();

    public abstract String getNomeExibicao();

    public abstract String getDocumento();

    private static String validarCelular(String celular) {
        if (celular == null || celular.isBlank()) {
            throw new RegraDeNegocioException("Celular é obrigatório");
        }
        return celular;
    }

    private static String validarEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new RegraDeNegocioException("Email inválido");
        }
        return email;
    }

    private static Endereco validarEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new RegraDeNegocioException("Endereço é obrigatório");
        }
        return endereco;
    }

    public UUID getId() {
        return id;
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

    public Endereco getEndereco() {
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
