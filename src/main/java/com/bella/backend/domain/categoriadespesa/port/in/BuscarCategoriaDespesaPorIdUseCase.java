package com.bella.backend.domain.categoriadespesa.port.in;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;

import java.util.UUID;

public interface BuscarCategoriaDespesaPorIdUseCase {

    CategoriaDespesa buscarPorId(UUID id);
}
