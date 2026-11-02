package com.bella.backend.domain.auditoria.port.out;

/**
 * Identifica quem está executando a operação corrente. O projeto ainda não tem autenticação; quando
 * tiver, basta trocar a implementação desta porta.
 */
public interface UsuarioResponsavelPort {

    String usuarioAtual();
}
