package com.bella.backend.domain.estoque.port.in;

import com.bella.backend.domain.estoque.model.MovimentacaoEstoque;

public interface RegistrarSaidaEstoqueUseCase {

    MovimentacaoEstoque registrarSaida(ComandoMovimentacaoManual comando);
}
