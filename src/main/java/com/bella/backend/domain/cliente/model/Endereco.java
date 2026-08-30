package com.bella.backend.domain.cliente.model;

public record Endereco(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf
) {
}
