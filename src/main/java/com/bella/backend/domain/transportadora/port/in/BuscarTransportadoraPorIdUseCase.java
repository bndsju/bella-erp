package com.bella.backend.domain.transportadora.port.in;

import com.bella.backend.domain.transportadora.model.Transportadora;

import java.util.UUID;

public interface BuscarTransportadoraPorIdUseCase {

    Transportadora buscarPorId(UUID id);
}
