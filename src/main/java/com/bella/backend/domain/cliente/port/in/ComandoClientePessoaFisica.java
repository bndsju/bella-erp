package com.bella.backend.domain.cliente.port.in;

import com.bella.backend.domain.cliente.model.Endereco;

import java.time.LocalDate;

public record ComandoClientePessoaFisica(
        String nomeCompleto,
        LocalDate dataNascimento,
        String cpf,
        String celular,
        String email,
        String telefone,
        Endereco endereco,
        String observacoes
) implements ComandoCliente {
}
