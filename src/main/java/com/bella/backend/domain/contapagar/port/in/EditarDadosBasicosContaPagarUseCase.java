package com.bella.backend.domain.contapagar.port.in;

import com.bella.backend.domain.contapagar.model.ContaPagar;

import java.util.UUID;

public interface EditarDadosBasicosContaPagarUseCase {

    ContaPagar editarDadosBasicos(UUID id, ComandoDadosBasicosContaPagar comando);
}
