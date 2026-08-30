package com.bella.backend.domain.cliente.port.in;

import com.bella.backend.domain.cliente.model.Endereco;

public record ComandoClientePessoaJuridica(
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String inscricaoEstadual,
        String inscricaoMunicipal,
        String celular,
        String email,
        String telefone,
        Endereco endereco,
        String observacoes
) implements ComandoCliente {
}
