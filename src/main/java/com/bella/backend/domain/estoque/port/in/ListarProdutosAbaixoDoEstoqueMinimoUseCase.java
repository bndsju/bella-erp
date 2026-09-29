package com.bella.backend.domain.estoque.port.in;

import com.bella.backend.domain.estoque.model.PosicaoEstoqueProduto;

import java.util.List;

public interface ListarProdutosAbaixoDoEstoqueMinimoUseCase {

    List<PosicaoEstoqueProduto> listarAbaixoDoMinimo();
}
