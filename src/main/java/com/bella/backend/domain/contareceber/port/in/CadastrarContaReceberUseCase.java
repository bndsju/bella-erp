package com.bella.backend.domain.contareceber.port.in;

import com.bella.backend.domain.contareceber.model.ContaReceber;

public interface CadastrarContaReceberUseCase {

    ContaReceber cadastrar(ComandoContaReceber comando);
}
