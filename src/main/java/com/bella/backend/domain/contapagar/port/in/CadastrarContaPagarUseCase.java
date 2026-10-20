package com.bella.backend.domain.contapagar.port.in;

import com.bella.backend.domain.contapagar.model.ContaPagar;

public interface CadastrarContaPagarUseCase {

    ContaPagar cadastrar(ComandoContaPagar comando);
}
