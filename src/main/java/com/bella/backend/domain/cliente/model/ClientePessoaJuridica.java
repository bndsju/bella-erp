package com.bella.backend.domain.cliente.model;

import com.bella.backend.domain.cliente.validation.CnpjValidator;
import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;

public final class ClientePessoaJuridica extends Cliente {

    private String razaoSocial;
    private String nomeFantasia;
    private final String cnpj;
    private String inscricaoEstadual;
    private String inscricaoMunicipal;

    private ClientePessoaJuridica(UUID id, String razaoSocial, String nomeFantasia, String cnpj,
                                   String inscricaoEstadual, String inscricaoMunicipal,
                                   String celular, String email, String telefone, Endereco endereco,
                                   String observacoes, StatusCliente status, Instant criadoEm,
                                   Instant atualizadoEm) {
        super(id, celular, email, telefone, endereco, observacoes, status, criadoEm, atualizadoEm);
        this.razaoSocial = validarRazaoSocial(razaoSocial);
        this.nomeFantasia = nomeFantasia;
        this.cnpj = CnpjValidator.normalizarEValidar(cnpj);
        this.inscricaoEstadual = inscricaoEstadual;
        this.inscricaoMunicipal = inscricaoMunicipal;
    }

    public static ClientePessoaJuridica novo(String razaoSocial, String nomeFantasia, String cnpj,
                                              String inscricaoEstadual, String inscricaoMunicipal,
                                              String celular, String email, String telefone, Endereco endereco,
                                              String observacoes) {
        Instant agora = Instant.now();
        return new ClientePessoaJuridica(UUID.randomUUID(), razaoSocial, nomeFantasia, cnpj,
                inscricaoEstadual, inscricaoMunicipal, celular, email, telefone, endereco, observacoes,
                StatusCliente.ATIVO, agora, agora);
    }

    public static ClientePessoaJuridica existente(UUID id, String razaoSocial, String nomeFantasia, String cnpj,
                                                   String inscricaoEstadual, String inscricaoMunicipal,
                                                   String celular, String email, String telefone, Endereco endereco,
                                                   String observacoes, StatusCliente status, Instant criadoEm,
                                                   Instant atualizadoEm) {
        return new ClientePessoaJuridica(id, razaoSocial, nomeFantasia, cnpj, inscricaoEstadual, inscricaoMunicipal,
                celular, email, telefone, endereco, observacoes, status, criadoEm, atualizadoEm);
    }

    public void editar(String razaoSocial, String nomeFantasia, String inscricaoEstadual, String inscricaoMunicipal,
                        String celular, String email, String telefone, Endereco endereco, String observacoes) {
        this.razaoSocial = validarRazaoSocial(razaoSocial);
        this.nomeFantasia = nomeFantasia;
        this.inscricaoEstadual = inscricaoEstadual;
        this.inscricaoMunicipal = inscricaoMunicipal;
        atualizarDadosComuns(celular, email, telefone, endereco, observacoes);
    }

    private static String validarRazaoSocial(String razaoSocial) {
        if (razaoSocial == null || razaoSocial.isBlank()) {
            throw new RegraDeNegocioException("Razão social é obrigatória");
        }
        return razaoSocial;
    }

    @Override
    public TipoPessoa getTipoPessoa() {
        return TipoPessoa.PJ;
    }

    @Override
    public String getNomeExibicao() {
        return razaoSocial;
    }

    @Override
    public String getDocumento() {
        return cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public String getInscricaoMunicipal() {
        return inscricaoMunicipal;
    }
}
