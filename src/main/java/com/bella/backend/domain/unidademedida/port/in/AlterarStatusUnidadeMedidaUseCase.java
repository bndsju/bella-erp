package com.bella.backend.domain.unidademedida.port.in;

import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import com.bella.backend.domain.unidademedida.model.UnidadeMedida;

import java.util.UUID;

public interface AlterarStatusUnidadeMedidaUseCase {

    UnidadeMedida alterarStatus(UUID id, StatusUnidadeMedida novoStatus);
}
