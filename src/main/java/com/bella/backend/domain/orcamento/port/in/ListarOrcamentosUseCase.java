package com.bella.backend.domain.orcamento.port.in;

import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.StatusOrcamento;

import java.util.List;
import java.util.UUID;

public interface ListarOrcamentosUseCase {

    List<Orcamento> listar(FiltroListagem filtro);

    record FiltroListagem(UUID clienteId, StatusOrcamento status) {
    }
}
