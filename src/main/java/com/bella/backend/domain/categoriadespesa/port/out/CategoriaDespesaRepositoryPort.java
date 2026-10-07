package com.bella.backend.domain.categoriadespesa.port.out;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaDespesaRepositoryPort {

    CategoriaDespesa salvar(CategoriaDespesa categoriaDespesa);

    Optional<CategoriaDespesa> buscarPorId(UUID id);

    List<CategoriaDespesa> listar(StatusCategoriaDespesa status);

    boolean existePorId(UUID id);
}
