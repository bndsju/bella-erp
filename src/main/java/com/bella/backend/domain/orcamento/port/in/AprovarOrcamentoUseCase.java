package com.bella.backend.domain.orcamento.port.in;

import com.bella.backend.domain.orcamento.model.Orcamento;

import java.util.UUID;

public interface AprovarOrcamentoUseCase {

    Orcamento aprovar(UUID id);
}
