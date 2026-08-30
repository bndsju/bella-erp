package com.bella.backend.domain.unidademedida.port.in;

import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import com.bella.backend.domain.unidademedida.model.UnidadeMedida;

import java.util.List;

public interface ListarUnidadesMedidaUseCase {

    List<UnidadeMedida> listar(StatusUnidadeMedida status);
}
