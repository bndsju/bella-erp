package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;

public interface LancarMovimentacaoManualCaixaUseCase {

    MovimentacaoCaixa lancar(ComandoLancamentoManualCaixa comando);
}
