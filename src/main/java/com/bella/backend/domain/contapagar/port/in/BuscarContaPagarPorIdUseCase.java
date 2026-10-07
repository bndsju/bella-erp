package com.bella.backend.domain.contapagar.port.in;

import com.bella.backend.domain.contapagar.model.ContaPagar;

import java.util.UUID;

public interface BuscarContaPagarPorIdUseCase {

    ContaPagar buscarPorId(UUID id);
}
