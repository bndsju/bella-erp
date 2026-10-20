package com.bella.backend.domain.categoriafluxocaixa.port.out;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaFluxoCaixaRepositoryPort {

    CategoriaFluxoCaixa salvar(CategoriaFluxoCaixa categoriaFluxoCaixa);

    Optional<CategoriaFluxoCaixa> buscarPorId(UUID id);

    List<CategoriaFluxoCaixa> listar(StatusCategoriaFluxoCaixa status);

    boolean existePorId(UUID id);
}
