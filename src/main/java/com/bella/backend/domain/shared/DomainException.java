package com.bella.backend.domain.shared;

/**
 * Exceção base para violações de regras de negócio no domínio.
 * Não deve depender de nenhuma tecnologia externa (Spring, JPA, etc).
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
