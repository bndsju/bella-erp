package com.bella.backend.domain.fornecedor.port.in;

import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import com.bella.backend.domain.fornecedor.model.Fornecedor;

import java.util.UUID;

public interface AlterarStatusFornecedorUseCase {

    Fornecedor alterarStatus(UUID id, StatusFornecedor novoStatus);
}
