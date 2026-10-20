package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;

import java.util.UUID;

public interface BuscarMovimentacaoCaixaPorIdUseCase {

    MovimentacaoCaixa buscarPorId(UUID id);
}
