package com.bella.backend.domain.fornecedor.port.in;

import com.bella.backend.domain.fornecedor.model.Fornecedor;

import java.util.UUID;

public interface BuscarFornecedorPorIdUseCase {

    Fornecedor buscarPorId(UUID id);
}
