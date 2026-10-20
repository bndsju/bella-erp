package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;

import java.util.UUID;

/** Cancela a movimentação quando o registro de origem é cancelado, mantendo a consistência do fluxo. */
public interface CancelarMovimentacaoAutomaticaCaixaUseCase {

    MovimentacaoCaixa cancelar(OrigemMovimentacaoCaixa origem, UUID origemReferenciaId, String motivo);
}
