package com.bella.backend.adapter.in.web.cliente;

import com.bella.backend.domain.cliente.model.Endereco;

public record EnderecoResponse(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf
) {

    public static EnderecoResponse de(Endereco endereco) {
        return new EnderecoResponse(endereco.cep(), endereco.logradouro(), endereco.numero(),
                endereco.complemento(), endereco.bairro(), endereco.cidade(), endereco.uf());
    }
}
