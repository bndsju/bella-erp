package com.bella.backend.domain.categoria.port.in;

import com.bella.backend.domain.categoria.model.Categoria;

import java.util.UUID;

public interface BuscarCategoriaPorIdUseCase {

    Categoria buscarPorId(UUID id);
}
