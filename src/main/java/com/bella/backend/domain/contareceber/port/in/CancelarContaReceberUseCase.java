package com.bella.backend.domain.contareceber.port.in;

import com.bella.backend.domain.contareceber.model.ContaReceber;

import java.util.UUID;

public interface CancelarContaReceberUseCase {

    ContaReceber cancelar(UUID id, String motivo);
}
