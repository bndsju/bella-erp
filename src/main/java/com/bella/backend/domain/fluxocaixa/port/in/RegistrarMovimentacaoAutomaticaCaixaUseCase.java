package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;

/**
 * Porta para os módulos de origem. Uma operação de origem (origem + referência) gera uma única
 * movimentação: registrar duas vezes a mesma origem/referência é rejeitado.
 */
public interface RegistrarMovimentacaoAutomaticaCaixaUseCase {

    MovimentacaoCaixa registrar(ComandoMovimentacaoAutomaticaCaixa comando);
}
