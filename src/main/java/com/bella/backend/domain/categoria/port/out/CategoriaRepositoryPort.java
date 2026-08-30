package com.bella.backend.domain.categoria.port.out;

import com.bella.backend.domain.categoria.model.Categoria;
import com.bella.backend.domain.categoria.model.StatusCategoria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepositoryPort {

    Categoria salvar(Categoria categoria);

    Optional<Categoria> buscarPorId(UUID id);

    List<Categoria> listar(StatusCategoria status);

    boolean existePorId(UUID id);
}
