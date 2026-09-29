package com.bella.backend.domain.estoque.port.out;

import com.bella.backend.domain.estoque.model.SaldoEstoque;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaldoEstoqueRepositoryPort {

    SaldoEstoque salvar(SaldoEstoque saldo);

    Optional<SaldoEstoque> buscarPorProdutoId(UUID produtoId);

    List<SaldoEstoque> listarTodos();
}
