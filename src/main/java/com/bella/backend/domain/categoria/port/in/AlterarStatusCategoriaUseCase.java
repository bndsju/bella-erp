package com.bella.backend.domain.categoria.port.in;

import com.bella.backend.domain.categoria.model.Categoria;
import com.bella.backend.domain.categoria.model.StatusCategoria;

import java.util.UUID;

public interface AlterarStatusCategoriaUseCase {

    Categoria alterarStatus(UUID id, StatusCategoria novoStatus);
}
