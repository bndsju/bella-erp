package com.bella.backend.domain.transportadora.port.out;

import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import com.bella.backend.domain.transportadora.model.Transportadora;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransportadoraRepositoryPort {

    Transportadora salvar(Transportadora transportadora);

    Optional<Transportadora> buscarPorId(UUID id);

    List<Transportadora> listar(StatusTransportadora status);

    boolean existePorId(UUID id);
}
