package com.bella.backend.application.auditoria;

import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.auditoria.model.OperacaoAuditoria;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um método de serviço de aplicação cuja execução deve ser auditada. O registro é criado pelo
 * {@link AuditoriaAspect}, na mesma transação da operação: se a auditoria falhar, a operação também é
 * desfeita. Os métodos auditados devem retornar o registro afetado, e, exceto nas criações, receber
 * o identificador dele como primeiro argumento.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditado {

    ModuloAuditoria modulo();

    OperacaoAuditoria operacao();
}
