package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;

import java.time.LocalDate;
import java.util.UUID;

public interface RealizarMovimentacaoManualCaixaUseCase {

    /** Se a data efetiva for nula, assume a data de hoje. */
    MovimentacaoCaixa realizar(UUID id, LocalDate dataEfetiva);
}
