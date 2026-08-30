package com.bella.backend.domain.produto.port.in;

import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.model.StatusProduto;

import java.util.UUID;

public interface AlterarStatusProdutoUseCase {

    Produto alterarStatus(UUID id, StatusProduto novoStatus);
}
