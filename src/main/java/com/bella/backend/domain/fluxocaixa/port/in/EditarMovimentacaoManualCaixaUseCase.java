package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;

import java.util.UUID;

public interface EditarMovimentacaoManualCaixaUseCase {

    MovimentacaoCaixa editar(UUID id, ComandoLancamentoManualCaixa comando);
}
