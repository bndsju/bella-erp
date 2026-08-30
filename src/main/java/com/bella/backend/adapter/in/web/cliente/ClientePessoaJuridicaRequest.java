package com.bella.backend.adapter.in.web.cliente;

import com.bella.backend.domain.cliente.port.in.ComandoCliente;
import com.bella.backend.domain.cliente.port.in.ComandoClientePessoaJuridica;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClientePessoaJuridicaRequest(
        @NotBlank(message = "Razão social é obrigatória") String razaoSocial,
        String nomeFantasia,
        @NotBlank(message = "CNPJ é obrigatório") String cnpj,
        String inscricaoEstadual,
        String inscricaoMunicipal,
        @NotBlank(message = "Celular é obrigatório") String celular,
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,
        String telefone,
        @Valid @NotNull(message = "Endereço é obrigatório") EnderecoRequest endereco,
        String observacoes
) implements ClienteRequest {

    @Override
    public ComandoCliente paraComando() {
        return new ComandoClientePessoaJuridica(razaoSocial, nomeFantasia, cnpj, inscricaoEstadual,
                inscricaoMunicipal, celular, email, telefone, endereco.paraDominio(), observacoes);
    }
}
