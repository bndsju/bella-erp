package com.bella.backend.domain.produto.port.in;

import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.model.StatusProduto;

import java.util.List;
import java.util.UUID;

public interface ListarProdutosUseCase {

    List<Produto> listar(FiltroListagem filtro);

    record FiltroListagem(String termoBusca, StatusProduto status, UUID categoriaId) {
    }
}
