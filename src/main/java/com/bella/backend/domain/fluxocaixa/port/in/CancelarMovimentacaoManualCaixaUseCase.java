package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;

import java.util.UUID;

public interface CancelarMovimentacaoManualCaixaUseCase {

    /** Cancelamento (se prevista) ou estorno (se realizada) de um lançamento manual. */
    MovimentacaoCaixa cancelar(UUID id, String motivo);
}
