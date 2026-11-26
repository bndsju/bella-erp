package com.bella.backend.domain.usuario.port.in;

import com.bella.backend.domain.usuario.model.Usuario;

import java.time.Instant;

public interface AutenticarUseCase {

    Sessao autenticar(Comando comando);

    /** {@code enderecoIp} só alimenta o registro de segurança; pode ser nulo. */
    record Comando(String login, String senha, String enderecoIp) {
        public Comando(String login, String senha) {
            this(login, senha, null);
        }
    }

    record Sessao(String token, Instant expiraEm, Usuario usuario) {
    }
}
