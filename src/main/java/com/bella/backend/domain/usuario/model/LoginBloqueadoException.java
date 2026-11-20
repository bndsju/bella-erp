package com.bella.backend.domain.usuario.model;

import com.bella.backend.domain.shared.DomainException;

/** Muitas tentativas de login erradas em sequência: o login fica bloqueado por um tempo. */
public class LoginBloqueadoException extends DomainException {

    public LoginBloqueadoException(long minutos) {
        super("Muitas tentativas de login. Tente de novo em " + minutos + " minuto(s)");
    }
}
