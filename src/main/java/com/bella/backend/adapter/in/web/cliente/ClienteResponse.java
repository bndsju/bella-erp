package com.bella.backend.adapter.in.web.cliente;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.model.ClientePessoaFisica;
import com.bella.backend.domain.cliente.model.ClientePessoaJuridica;
import com.bella.backend.domain.cliente.model.StatusCliente;
import com.bella.backend.domain.cliente.model.TipoPessoa;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        TipoPessoa tipoPessoa,
        StatusCliente status,

        String nomeCompleto,
        LocalDate dataNascimento,
        String cpf,

        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String inscricaoEstadual,
        String inscricaoMunicipal,

        String telefone,
        String celular,
        String email,
        EnderecoResponse endereco,
        String observacoes,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static ClienteResponse de(Cliente cliente) {
        EnderecoResponse endereco = EnderecoResponse.de(cliente.getEndereco());

        return switch (cliente) {
            case ClientePessoaFisica pf -> new ClienteResponse(
                    pf.getId(), TipoPessoa.PF, pf.getStatus(),
                    pf.getNomeCompleto(), pf.getDataNascimento(), pf.getCpf(),
                    null, null, null, null, null,
                    pf.getTelefone(), pf.getCelular(), pf.getEmail(), endereco, pf.getObservacoes(),
                    pf.getCriadoEm(), pf.getAtualizadoEm());
            case ClientePessoaJuridica pj -> new ClienteResponse(
                    pj.getId(), TipoPessoa.PJ, pj.getStatus(),
                    null, null, null,
                    pj.getRazaoSocial(), pj.getNomeFantasia(), pj.getCnpj(), pj.getInscricaoEstadual(),
                    pj.getInscricaoMunicipal(),
                    pj.getTelefone(), pj.getCelular(), pj.getEmail(), endereco, pj.getObservacoes(),
                    pj.getCriadoEm(), pj.getAtualizadoEm());
        };
    }
}
