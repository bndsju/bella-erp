package com.bella.backend.domain.estoque.port.in;

import com.bella.backend.domain.estoque.model.MovimentacaoEstoque;

public interface RegistrarAjusteEntradaEstoqueUseCase {

    MovimentacaoEstoque registrarAjusteEntrada(ComandoMovimentacaoManual comando);
}
