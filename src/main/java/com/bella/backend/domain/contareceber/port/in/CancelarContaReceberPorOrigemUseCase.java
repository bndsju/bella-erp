package com.bella.backend.domain.contareceber.port.in;

import com.bella.backend.domain.contareceber.model.OrigemContaReceber;

import java.util.UUID;

/** Cancela a conta gerada por outro módulo (ex.: venda cancelada). Sem conta de origem, nada a fazer. */
public interface CancelarContaReceberPorOrigemUseCase {

    void cancelarPorOrigem(OrigemContaReceber origem, UUID origemReferenciaId, String motivo);
}
