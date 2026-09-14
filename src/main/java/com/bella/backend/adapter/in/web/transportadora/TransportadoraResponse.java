package com.bella.backend.adapter.in.web.transportadora;

import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import com.bella.backend.domain.transportadora.model.Transportadora;

import java.time.Instant;
import java.util.UUID;

public record TransportadoraResponse(
        UUID id,
        String nome,
        String telefone,
        StatusTransportadora status,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static TransportadoraResponse de(Transportadora transportadora) {
        return new TransportadoraResponse(transportadora.getId(), transportadora.getNome(),
                transportadora.getTelefone(), transportadora.getStatus(), transportadora.getCriadoEm(),
                transportadora.getAtualizadoEm());
    }
}
