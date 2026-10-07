package com.bella.backend.domain.fornecedor.port.in;

import com.bella.backend.domain.fornecedor.model.Fornecedor;

import java.util.UUID;

public interface EditarFornecedorUseCase {

    Fornecedor editar(UUID id, Comando comando);

    record Comando(String nome, String telefone) {
    }
}
