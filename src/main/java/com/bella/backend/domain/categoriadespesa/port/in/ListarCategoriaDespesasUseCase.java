package com.bella.backend.domain.categoriadespesa.port.in;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;

import java.util.List;

public interface ListarCategoriaDespesasUseCase {

    List<CategoriaDespesa> listar(StatusCategoriaDespesa status);
}
