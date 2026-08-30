package com.bella.backend.domain.produto.port.in;

import com.bella.backend.domain.produto.model.Produto;

import java.util.UUID;

public interface BuscarProdutoPorIdUseCase {

    Produto buscarPorId(UUID id);
}
