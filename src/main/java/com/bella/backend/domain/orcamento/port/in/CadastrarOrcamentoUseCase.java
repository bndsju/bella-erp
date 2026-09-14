package com.bella.backend.domain.orcamento.port.in;

import com.bella.backend.domain.orcamento.model.Orcamento;

public interface CadastrarOrcamentoUseCase {

    Orcamento cadastrar(ComandoOrcamento comando);
}
