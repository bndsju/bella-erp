package com.bella.backend.domain.usuario.port.in;

import com.bella.backend.domain.usuario.model.Usuario;

import java.time.Instant;

public interface AutenticarUseCase {

    Sessao autenticar(Comando comando);

    record Comando(String login, String senha) {
    }

    record Sessao(String token, Instant expiraEm, Usuario usuario) {
    }
}
