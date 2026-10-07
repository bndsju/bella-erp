package com.bella.backend.domain.fornecedor.port.in;

import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import com.bella.backend.domain.fornecedor.model.Fornecedor;

import java.util.List;

public interface ListarFornecedoresUseCase {

    List<Fornecedor> listar(StatusFornecedor status);
}
