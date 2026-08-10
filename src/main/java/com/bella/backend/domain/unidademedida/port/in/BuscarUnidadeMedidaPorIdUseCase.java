package com.bella.backend.domain.unidademedida.port.in;

import com.bella.backend.domain.unidademedida.model.UnidadeMedida;

import java.util.UUID;

public interface BuscarUnidadeMedidaPorIdUseCase {

    UnidadeMedida buscarPorId(UUID id);
}
