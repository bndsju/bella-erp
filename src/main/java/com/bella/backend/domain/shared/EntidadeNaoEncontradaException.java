package com.bella.backend.domain.shared;

public class EntidadeNaoEncontradaException extends DomainException {

    public EntidadeNaoEncontradaException(String entidade, Object id) {
        super("%s não encontrado(a) para o id: %s".formatted(entidade, id));
    }
}
