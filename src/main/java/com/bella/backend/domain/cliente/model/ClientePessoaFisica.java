package com.bella.backend.domain.cliente.model;

import com.bella.backend.domain.cliente.validation.CpfValidator;
import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class ClientePessoaFisica extends Cliente {

    private String nomeCompleto;
    private LocalDate dataNascimento;
    private final String cpf;

    private ClientePessoaFisica(UUID id, String nomeCompleto, LocalDate dataNascimento, String cpf,
                                 String celular, String email, String telefone, Endereco endereco,
                                 String observacoes, StatusCliente status, Instant criadoEm, Instant atualizadoEm) {
        super(id, celular, email, telefone, endereco, observacoes, status, criadoEm, atualizadoEm);
        this.nomeCompleto = validarNomeCompleto(nomeCompleto);
        this.dataNascimento = validarDataNascimento(dataNascimento);
        this.cpf = CpfValidator.normalizarEValidar(cpf);
    }

    public static ClientePessoaFisica novo(String nomeCompleto, LocalDate dataNascimento, String cpf,
                                            String celular, String email, String telefone, Endereco endereco,
                                            String observacoes) {
        Instant agora = Instant.now();
        return new ClientePessoaFisica(UUID.randomUUID(), nomeCompleto, dataNascimento, cpf,
                celular, email, telefone, endereco, observacoes, StatusCliente.ATIVO, agora, agora);
    }

    public static ClientePessoaFisica existente(UUID id, String nomeCompleto, LocalDate dataNascimento, String cpf,
                                                 String celular, String email, String telefone, Endereco endereco,
                                                 String observacoes, StatusCliente status, Instant criadoEm,
                                                 Instant atualizadoEm) {
        return new ClientePessoaFisica(id, nomeCompleto, dataNascimento, cpf, celular, email, telefone, endereco,
                observacoes, status, criadoEm, atualizadoEm);
    }

    public void editar(String nomeCompleto, LocalDate dataNascimento, String celular, String email,
                        String telefone, Endereco endereco, String observacoes) {
        this.nomeCompleto = validarNomeCompleto(nomeCompleto);
        this.dataNascimento = validarDataNascimento(dataNascimento);
        atualizarDadosComuns(celular, email, telefone, endereco, observacoes);
    }

    private static String validarNomeCompleto(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new RegraDeNegocioException("Nome completo é obrigatório");
        }
        return nomeCompleto;
    }

    private static LocalDate validarDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new RegraDeNegocioException("Data de nascimento é obrigatória para Pessoa Física");
        }
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new RegraDeNegocioException("Data de nascimento não pode ser uma data futura");
        }
        return dataNascimento;
    }

    @Override
    public TipoPessoa getTipoPessoa() {
        return TipoPessoa.PF;
    }

    @Override
    public String getNomeExibicao() {
        return nomeCompleto;
    }

    @Override
    public String getDocumento() {
        return cpf;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getCpf() {
        return cpf;
    }
}
