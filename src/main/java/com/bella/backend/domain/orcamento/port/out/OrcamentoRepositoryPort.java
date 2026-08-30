package com.bella.backend.domain.orcamento.port.out;

import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.port.in.ListarOrcamentosUseCase.FiltroListagem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoRepositoryPort {

    Orcamento salvar(Orcamento orcamento);

    Optional<Orcamento> buscarPorId(UUID id);

    List<Orcamento> listar(FiltroListagem filtro);

    boolean existePorId(UUID id);
}
