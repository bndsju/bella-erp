package com.bella.backend.domain.fornecedor.port.in;

import com.bella.backend.domain.fornecedor.model.Fornecedor;

public interface CadastrarFornecedorUseCase {

    Fornecedor cadastrar(Comando comando);

    record Comando(String nome, String telefone) {
    }
}
