package com.bella.backend.domain.permissao.port.in;

import com.bella.backend.domain.permissao.model.AcaoPermissao;
import com.bella.backend.domain.permissao.model.ModuloSistema;
import com.bella.backend.domain.usuario.model.PerfilUsuario;

import java.util.Map;
import java.util.Set;

public interface ConsultarPermissoesUseCase {

    /** Permissões de um perfil. O administrador sempre tem todas. */
    Map<ModuloSistema, Set<AcaoPermissao>> doPerfil(PerfilUsuario perfil);

    /** A matriz inteira, um mapa por perfil (inclui o administrador, com tudo). */
    Map<PerfilUsuario, Map<ModuloSistema, Set<AcaoPermissao>>> todas();

    boolean permite(PerfilUsuario perfil, ModuloSistema modulo, AcaoPermissao acao);
}
