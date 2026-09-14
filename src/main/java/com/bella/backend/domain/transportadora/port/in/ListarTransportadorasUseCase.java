package com.bella.backend.domain.transportadora.port.in;

import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import com.bella.backend.domain.transportadora.model.Transportadora;

import java.util.List;

public interface ListarTransportadorasUseCase {

    List<Transportadora> listar(StatusTransportadora status);
}
