package com.bella.backend.adapter.in.web.cliente;

import com.bella.backend.domain.cliente.port.in.ComandoCliente;
import com.bella.backend.domain.cliente.port.in.ComandoClientePessoaFisica;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record ClientePessoaFisicaRequest(
        @NotBlank(message = "Nome completo é obrigatório") String nomeCompleto,
        @NotNull(message = "Data de nascimento é obrigatória")
        @PastOrPresent(message = "Data de nascimento não pode ser uma data futura") LocalDate dataNascimento,
        @NotBlank(message = "CPF é obrigatório") String cpf,
        @NotBlank(message = "Celular é obrigatório") String celular,
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,
        String telefone,
        @Valid @NotNull(message = "Endereço é obrigatório") EnderecoRequest endereco,
        String observacoes
) implements ClienteRequest {

    @Override
    public ComandoCliente paraComando() {
        return new ComandoClientePessoaFisica(nomeCompleto, dataNascimento, cpf, celular, email, telefone,
                endereco.paraDominio(), observacoes);
    }
}
