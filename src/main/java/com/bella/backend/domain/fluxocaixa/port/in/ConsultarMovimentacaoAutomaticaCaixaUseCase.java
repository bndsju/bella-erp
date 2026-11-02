package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;

import java.util.Optional;
import java.util.UUID;

public interface ConsultarMovimentacaoAutomaticaCaixaUseCase {

    Optional<MovimentacaoCaixa> consultarPorOrigem(OrigemMovimentacaoCaixa origem, UUID origemReferenciaId);
}
