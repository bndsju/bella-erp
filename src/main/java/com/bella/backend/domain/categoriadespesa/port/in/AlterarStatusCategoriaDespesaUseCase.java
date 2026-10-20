package com.bella.backend.domain.categoriadespesa.port.in;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;

import java.util.UUID;

public interface AlterarStatusCategoriaDespesaUseCase {

    CategoriaDespesa alterarStatus(UUID id, StatusCategoriaDespesa novoStatus);
}
