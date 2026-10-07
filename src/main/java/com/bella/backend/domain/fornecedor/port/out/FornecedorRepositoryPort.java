package com.bella.backend.domain.fornecedor.port.out;

import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import com.bella.backend.domain.fornecedor.model.Fornecedor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FornecedorRepositoryPort {

    Fornecedor salvar(Fornecedor fornecedor);

    Optional<Fornecedor> buscarPorId(UUID id);

    List<Fornecedor> listar(StatusFornecedor status);

    boolean existePorId(UUID id);
}
