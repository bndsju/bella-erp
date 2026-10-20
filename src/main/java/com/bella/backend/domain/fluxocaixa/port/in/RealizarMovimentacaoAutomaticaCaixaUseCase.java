package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;

import java.time.LocalDate;
import java.util.UUID;

/** Efetiva (prevista -> realizada) a movimentação do registro de origem, sem criar outra. */
public interface RealizarMovimentacaoAutomaticaCaixaUseCase {

    MovimentacaoCaixa realizar(OrigemMovimentacaoCaixa origem, UUID origemReferenciaId, LocalDate dataEfetiva);
}
