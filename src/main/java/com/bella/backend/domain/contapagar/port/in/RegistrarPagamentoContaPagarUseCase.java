package com.bella.backend.domain.contapagar.port.in;

import com.bella.backend.domain.contapagar.model.ContaPagar;

import java.util.UUID;

public interface RegistrarPagamentoContaPagarUseCase {

    /** Se a data do pagamento for nula, assume a data de hoje. */
    ContaPagar registrarPagamento(UUID contaId, ComandoPagamentoContaPagar comando);
}
