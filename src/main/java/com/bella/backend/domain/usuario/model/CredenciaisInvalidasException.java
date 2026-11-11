package com.bella.backend.domain.usuario.model;

import com.bella.backend.domain.shared.DomainException;

/** Login ou senha incorretos, ou usuário inativo. A mensagem é a mesma de propósito, para não revelar qual foi. */
public class CredenciaisInvalidasException extends DomainException {

    public CredenciaisInvalidasException() {
        super("Login ou senha inválidos");
    }
}
