package com.bella.backend.domain.usuario.port.out;

import com.bella.backend.domain.usuario.model.Usuario;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmissorTokenPort {

    TokenEmitido emitir(Usuario usuario);

    /** Valida assinatura e validade. Vazio se o token for inválido, adulterado ou expirado. */
    Optional<DadosToken> validar(String token);

    record TokenEmitido(String token, Instant expiraEm) {
    }

    record DadosToken(UUID usuarioId, Instant emitidoEm) {
    }
}
