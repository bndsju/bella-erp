package com.bella.backend.domain.estoque.port.in;

import com.bella.backend.domain.estoque.model.PosicaoEstoqueProduto;

import java.util.UUID;

public interface ConsultarSaldoEstoqueUseCase {

    PosicaoEstoqueProduto buscarPorProdutoId(UUID produtoId);
}
