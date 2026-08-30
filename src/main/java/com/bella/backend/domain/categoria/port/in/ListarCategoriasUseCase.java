package com.bella.backend.domain.categoria.port.in;

import com.bella.backend.domain.categoria.model.Categoria;
import com.bella.backend.domain.categoria.model.StatusCategoria;

import java.util.List;

public interface ListarCategoriasUseCase {

    List<Categoria> listar(StatusCategoria status);
}
