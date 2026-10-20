package com.bella.backend.domain.categoriafluxocaixa.port.in;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;

import java.util.List;

public interface ListarCategoriaFluxoCaixasUseCase {

    List<CategoriaFluxoCaixa> listar(StatusCategoriaFluxoCaixa status);
}
