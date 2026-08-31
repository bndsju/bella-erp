package com.bella.backend.domain.transportadora.port.in;

import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import com.bella.backend.domain.transportadora.model.Transportadora;

import java.util.UUID;

public interface AlterarStatusTransportadoraUseCase {

    Transportadora alterarStatus(UUID id, StatusTransportadora novoStatus);
}
