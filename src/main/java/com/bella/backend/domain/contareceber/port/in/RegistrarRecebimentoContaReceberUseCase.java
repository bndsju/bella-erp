package com.bella.backend.domain.contareceber.port.in;

import com.bella.backend.domain.contareceber.model.ContaReceber;

import java.util.UUID;

public interface RegistrarRecebimentoContaReceberUseCase {

    /** Se a data do recebimento for nula, assume a data de hoje. */
    ContaReceber registrarRecebimento(UUID contaId, ComandoRecebimentoContaReceber comando);
}
