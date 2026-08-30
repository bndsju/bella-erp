package com.bella.backend.adapter.in.web.cliente;

import com.bella.backend.domain.cliente.model.Endereco;
import jakarta.validation.constraints.NotBlank;

public record EnderecoRequest(
        @NotBlank(message = "CEP é obrigatório") String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf
) {

    public Endereco paraDominio() {
        return new Endereco(cep, logradouro, numero, complemento, bairro, cidade, uf);
    }
}
