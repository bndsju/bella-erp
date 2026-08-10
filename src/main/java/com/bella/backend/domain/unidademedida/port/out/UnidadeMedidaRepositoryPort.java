package com.bella.backend.domain.unidademedida.port.out;

import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import com.bella.backend.domain.unidademedida.model.UnidadeMedida;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnidadeMedidaRepositoryPort {

    UnidadeMedida salvar(UnidadeMedida unidadeMedida);

    Optional<UnidadeMedida> buscarPorId(UUID id);

    List<UnidadeMedida> listar(StatusUnidadeMedida status);

    boolean existePorId(UUID id);
}
